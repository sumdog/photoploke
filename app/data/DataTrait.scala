package data

import play.api.mvc.MultipartFormData.FilePart
import models.Photo

trait DataTrait {

  def retrievePhoto(objectId : String, width : Option[Int]) : Option[Photo]
  
  def savePhoto(photo : Photo) : String
}