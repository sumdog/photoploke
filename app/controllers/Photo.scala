package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import util.CustomParsers

import java.io.File
import play.api.libs.json.Json
import views.html.photo.uploadForm

import com.mongodb.casbah.gridfs.GridFSDBFile

import play.core.Router
import data.DataTrait
import data.MongoData

object Photo extends Controller {
  

  /**
   * #Photo Functions (Admin)
GET     /admin/photos/upload        controllers.Photo.uploadForm
POST    /admin/photos/upload        controllers.Photo.upload
GET     /admin/photos/new           controllers.Photo.uninitialized
PUT     /admin/photos/:objectId     controllers.Photo.updateMetaData(objectId : String) 
GET     /admin/photos/editAjax/:objectId  controllers.Photo.editFormAjax\

#Photo Functions (Public)
GET     /photos/tagsJson            controllers.Photo.getTagsJson(objectId: String)
GET     /photo/:objectId/:width     controllers.Photo.getPhoto
   */

  //TODO Dependency Inject?
  val data : DataTrait = new MongoData()
  
  def uploadForm = Action { request => Ok(views.html.photo.uploadForm() ) }
  
  
  def upload = Action(CustomParsers.multipartFormDataAsBytes) { implicit request =>
	  
    request.body.file("qqfile").map { upload =>
    
      val objectId = data.savePhoto(models.Photo(upload.ref,"image/jpeg",upload.filename))
      val url = routes.Photo.getPhoto(objectId,1).absoluteURL()


      Created(Json.toJson(Map( "success" -> "true" ))).withHeaders( LOCATION -> url )
    }.getOrElse {
      Ok(Json.toJson(Map( "error" -> "Error uploading file")))
    }
  }
  
  
  def uninitialized = Action { request => NotImplemented }
  def updateMetaData( objectId : String) = Action { request => NotImplemented }
  def editFormAjax ( objectId : String) = Action { request => NotImplemented }
  def getTagsJson( objectId : String ) = Action { request => NotImplemented }
  
  def getPhoto(objectId: String, width : Int) = Action { request => 
    data.retrievePhoto(objectId, Some(width)) match {
      case Some(photo : models.Photo) => { Ok(photo.data).as(photo.contentType) }
      case None => { NotFound("Photo Not Found in GridFS") }
    }
  }
  
}