package controllers


import com.mongodb.casbah.commons.MongoDBObject
//import models.Photo
import play.api._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import java.io.File
import util.CustomParsers
import java.io.FileOutputStream
import play.api.mvc.MultipartFormData.FilePart
import play.Logger
import org.bson.types.ObjectId
import com.mongodb.casbah.gridfs.GridFSDBFile
import java.io.ByteArrayOutputStream
/*
object Administration extends Controller {

  val photoUploadForm : Form[Photo] = Form(
    mapping(
      "name" -> text
    )(Photo.apply)(Photo.unapply)
  )

  def uploadPhoto = Action {

    //val obj = MongoDBObject("somne_key" -> "some_value")
    //MongoObj.db("test1") += obj

    implicit request =>
      photoUploadForm.bindFromRequest.fold(
        errors => BadRequest(views.html.files(errors)),
        photoUpload => {
          val obj = MongoDBObject("name" -> photoUpload.name)
          MongoObj.db("photos") += obj
          Ok(views.html.files(photoUploadForm))
        }
      )



    Ok(views.html.files(photoUploadForm))

  }
  

	def fileUploader = Action(CustomParsers.multipartFormDataAsBytes) { request =>
	  
	  request.body.file("qqfile").map { upload =>
	    
	    val obj = MongoDBObject( "name" -> upload.filename  )
	    MongoObj.db("photos") += obj
	    
	    MongoObj.gridfs(upload.ref) { fh =>
	      fh.filename = new File("photos/",upload.filename).toString()
	      fh.contentType = "image/jpeg"
	    }
	    
	    Ok(Json.toJson(Map( "success" -> "true" )))
	  }.getOrElse {
	    Ok(Json.toJson(Map( "error" -> "Error uploading file")))
	  }
	  
	  /*request.body.files foreach {
	  	case FilePart(key, filename, contentType, bytes) => {
	  	  new FileOutputStream(new File("/tmp/foo",filename)).write(bytes)
	  	}// do something
	  }
	  
	  Ok(Json.toJson(Map( "success" -> "true" )))*/
	  
	  /*request.body.file("qqfile").map { picture =>

	    val filename = picture.filename 
	    val contentType = picture.contentType

	    new FileOutputStream("/tmp/test").write(picture.ref)
	    //picture.ref.moveTo(new File("/tmp",filename))
	    Ok(Json.toJson(Map( "success" -> "true" )))
	  }.getOrElse {
	    Ok(Json.toJson(Map( "error" -> "error occured")))
	  }*/
	}
	  
	
  def getPhoto(objectId : String) = Action {
    Logger.debug("ObjectId(%s)".format(objectId))
    val photo = MongoObj.db("photos").find(MongoDBObject("_id" -> new ObjectId(objectId)))
    
    if(photo.count < 1) {
      NotFound("Photo Not Found")
    }
    else {
      
      val filename = new File("photos/",photo.next.get("name").toString()).toString() 
      
      Logger.debug(filename)
      
      MongoObj.gridfs.findOne( filename  ) match {
        case Some(fs : GridFSDBFile) => {
          val out = new ByteArrayOutputStream()
          fs.writeTo(out)
          
          Ok(out.toByteArray()).as("image/jpeg")
        }
        case None => {
          NotFound("Photo Not Found in GridFS")
        }
      }
      
      //Ok(photo.next.get("photo").asInstanceOf[Array[Byte]]).as("image/jpeg")      
    }
    

  }
  
  def listPhotosJson = Action {
    
    //Ok(Json.toJson(MongoObj.db("photos").find()))
    
    var jsonList : List[JsValue] = Nil
    
    for (dbObject <- MongoObj.db("photos").find(MongoDBObject.empty, MongoDBObject("name" -> 1) )) {
    	jsonList ::=  Json.parse(dbObject.toString)
    }

    
    Ok(Json.toJson(jsonList)).as("application/json")
  }

  def index = Action {



    Ok(views.html.files(photoUploadForm))
  }

}*/
