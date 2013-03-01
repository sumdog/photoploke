package data

import com.mongodb.casbah._
import com.mongodb.casbah.gridfs.GridFS
import play.Logger
import java.net.UnknownHostException
import play.api.mvc.MultipartFormData.FilePart
import org.bson.types.ObjectId
import com.mongodb.casbah.gridfs.GridFSDBFile
import java.io.ByteArrayOutputStream
import com.mongodb.casbah.commons.MongoDBObject
import play.Application
import play.Application
import models.Photo
import java.io.File

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
  
  override def retrievePhoto(objectId : String, width : Option[Int]) : Option[Photo] = {
    
    val photo = MongoData.db(DOC_PHOTOS).find(MongoDBObject(ID -> new ObjectId(objectId)))
    
    if(photo.count < 1) {
      None
    }
    else {
      
      val photoInfo = photo.next

      MongoData.gridfs.findOne( new File(DOC_PHOTOS,photoInfo.get(NAME).toString()).toString() ) match {
        case Some(fs : GridFSDBFile) => {
          val out = new ByteArrayOutputStream()
          fs.writeTo(out)
          
          Some( 
              Photo(
                out.toByteArray(), 
                fs.contentType.getOrElse("application/octet-stream"),
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
      val objectId = new ObjectId()
      val obj = MongoDBObject( NAME -> photo.fileName , ID -> objectId )
      MongoData.db(DOC_PHOTOS) += obj
      MongoData.gridfs(photo.data) { fh =>
        fh.filename = new File(DOC_PHOTOS,photo.fileName).toString()
        fh.contentType = photo.contentType   
      }
      objectId.toString()
  }
  
}