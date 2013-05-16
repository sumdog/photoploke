package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.Files._
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData._
import java.io.File
import play.api.libs.json.{JsValue, Json}
import controllers.routes
import play.api.Logger


class PhotoManagementSpec extends Specification {

  
  "Application" should {

    var uploadObjectId : JsValue = null

    "Upload Photo" in {
    
    
        val file = scala.io.Source.fromFile(getClass().getResource("/photos/DSC03024.JPG").getFile())(scala.io.Codec.ISO8859).map(_.toByte).toArray
        
        val data = new MultipartFormData(Map(), List(
        FilePart("qqfile", "DSC03024.JPG", Some("image/jpeg"),
            file)
        ), List())
        
        val result = controllers.Photo.upload()(FakeRequest(POST, "/admin/photos/upload",FakeHeaders(),data))

        uploadObjectId = Json.parse(contentAsString(result)).\("objectId")

        status(result) must equalTo(CREATED)
        headers(result) must haveKeys(LOCATION)
        contentType(result) must beSome("application/json")      
        contentAsString(result) must /("success" -> "true")
    
      }


    "Retrieve Original Photo" in {

      running(FakeApplication()) {

      val url = routes.Photo.getOriginalPhoto(uploadObjectId.as[String]).url

        Logger.debug(url)

      val Some(result) = route(FakeRequest(GET,url))

      //Logger.debug(contentAsString(result))

      status(result) must equalTo(OK)
      contentType(result) must beSome("image/jpeg")
      }
    }


  }


  }