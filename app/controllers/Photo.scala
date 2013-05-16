package controllers

import play.api.mvc._
import util.{Constants, CustomParsers}

import java.io.File
import play.api.libs.json.Json
import views.html.photo.uploadForm

import com.mongodb.casbah.gridfs.GridFSDBFile

import play.core.Router
import data.DataTrait
import data.MongoData
import scala.Some
import play.Logger

object Photo extends Controller {

  //TODO Dependency Inject?
  val data : DataTrait = new MongoData()
  
  def uploadForm = Action { request => Ok(views.html.photo.uploadForm() ) }
  
  
  def upload = Action(CustomParsers.multipartFormDataAsBytes) { implicit request =>
	  
    request.body.file("qqfile").map { upload =>

      //TODO: restrict upload types, possibly by throwing error in conversation if it's not an image

      val objectId = data.savePhoto(models.Photo(upload.ref,upload.
        contentType match {
        case Some(s : String) => {s}
        case None => {Constants.UNKNOWN_MIME_TYPE}
        }
        ,upload.filename))
      val url = routes.Photo.getOriginalPhoto(objectId).absoluteURL()


      Created(Json.toJson(Map( "success" -> "true" , "location" -> url, "objectId" -> objectId ))).withHeaders( LOCATION -> url )
    }.getOrElse {
      Ok(Json.toJson(Map( "error" -> "Error uploading file")))
    }
  }

  
  def uninitialized = Action { request => NotImplemented }
  def updateMetaData( objectId : String) = Action { request => NotImplemented }
  def editFormAjax ( objectId : String) = Action { request => NotImplemented }
  def getTagsJson( objectId : String ) = Action { request => NotImplemented }

  def getOriginalPhoto(objectId: String) : Action[AnyContent] = getPhoto(objectId,None)
  def getPhotoResizedWatermarked(objectId: String, width : Int) : Action[AnyContent]  = getPhoto(objectId,Some(width))

  private def getPhoto(objectId: String, width : Option[Int]) = Action { request =>
    data.retrievePhoto(objectId, width) match {
      case Some(photo : models.Photo) => {  Ok(photo.data).as(photo.contentType) }
      //TODO standard Not Found?
      case None => { NotFound("Photo Not Found") }
    }
  }
  
}