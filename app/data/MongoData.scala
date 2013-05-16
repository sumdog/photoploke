package data

import _root_.util.{WaterMarkLocation, WaterMark, ImageTool, Constants}
import com.mongodb.casbah._
import com.mongodb.casbah.gridfs.GridFS
import play.Logger
import java.net.UnknownHostException
import play.api.mvc.MultipartFormData.FilePart
import org.bson.types.ObjectId
import com.mongodb.casbah.gridfs.GridFSDBFile
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import com.mongodb.casbah.commons.MongoDBObject
import play.Application
import play.Application
import models.Photo
import javax.imageio.ImageIO

object MongoData {
  private var client : MongoClient = _
  private var db : MongoDB = _
  private var gridfs : GridFS = _  
  
  def initalize() {
    Logger.debug("Loading MongoDB")
    try {

      MongoData.client = MongoClient(
          play.Configuration.root().getString("mongo.hostname"),
          play.Configuration.root().getInt("mongo.port")
      )
      MongoData.db = MongoData.client(play.Configuration.root().getString("mongo.database"))
      MongoData.gridfs = GridFS(MongoData.db)

    } catch {
      case e : UnknownHostException => { Logger.error("Unknown MongoDB Host",e)}
    }   
  }
  
}


class MongoData extends DataTrait {

  /**
   * Mongo document name for photo meta data as well as GridFS file path.
   */
  val DOC_PHOTOS = "photos"
    
  /**
   * standard Mongo ObjectId field.
   */  
  val ID = "_id"
    
  /**
   * original name of uploaded photo. 
   */  
  val NAME = "name"

  val SIZES = "sizes"
  
  override def retrievePhoto(objectId : String, width : Option[Int]) : Option[Photo] = {
    
    val photo = MongoData.db(DOC_PHOTOS).find(MongoDBObject(ID -> new ObjectId(objectId)))
    
    if(photo.count < 1) {
      None
    }
    else {
      
      val photoInfo = photo.next

      val file = width match {
        case Some(x : Int) => { new File(new File(DOC_PHOTOS,objectId.toString),x.toString) }
        case None => new File(DOC_PHOTOS,objectId.toString)
      }

      MongoData.gridfs.findOne( file.toString ) match {
        case Some(fs : GridFSDBFile) => {
          val out = new ByteArrayOutputStream()
          fs.writeTo(out)

          Some( 
              Photo(
                out.toByteArray(), 
                fs.contentType.getOrElse(Constants.UNKNOWN_MIME_TYPE),
                photoInfo.get(NAME).toString()
              ) 
          )
        }
        case None => {
          None
        }          
      }   
    }
  }
  
  override def savePhoto(photo : Photo) : String = {

    //unique ID for Image
    val objectId = new ObjectId()

    //save original
    MongoData.gridfs(photo.data) { fh =>
      fh.filename = new File(DOC_PHOTOS,objectId.toString).toString()
      fh.contentType = photo.contentType
    }

    //TODO: configurable
    val sizes = List( 4096 , 2048, 1024, 800, 640, 100, 50 )

    //TODO: use configurable
    val images = ImageTool.processPhotos(ImageIO.read(new ByteArrayInputStream(photo.data)),sizes,
      Some(WaterMark(
        scala.io.Source.fromFile(new File("/home/cassius/workspace/photoploke/journeyofkhan-watermark.svg")).map(_.toByte).toArray,
        0.30,
        WaterMarkLocation.TOP_CENTRE,
        10,10
      ))
    )

    //save reiszed/watermarked versions (includes watermarked version of original size)
    images.foreach {
      case(width,image) => {

        val out = new ByteArrayOutputStream()
        //TODO: setting (default JPEG)
        ImageIO.write(image,"jpg",out)

        MongoData.gridfs(out.toByteArray) { fh =>
          fh.filename =  new File(new File(DOC_PHOTOS,objectId.toString),width.toString()).toString
          fh.contentType = "image/jpeg"
        }
      }
    }

    //save metadata
    val obj = MongoDBObject( NAME -> photo.fileName , ID -> objectId , SIZES -> images.keys.toList )
    MongoData.db(DOC_PHOTOS) += obj

    objectId.toString()
  }
  
}