package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.Files._
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData._
import java.io.File


class PhotoManagementSpec extends Specification {

  
  "Application" should {

    "Upload Photo" in {
    
    
        val file = scala.io.Source.fromFile(getClass().getResource("/photos/DSC03024.JPG").getFile())(scala.io.Codec.ISO8859).map(_.toByte).toArray
        
        val data = new MultipartFormData(Map(), List(
        FilePart("qqfile", "message", Some("Content-Type: multipart/form-data"), 
            //TemporaryFile(getClass().getResource("/test/photos/DSC03024.JPG").getFile()))
            file)
        ), List())
        
        val result = controllers.Photo.upload()(FakeRequest(POST, "/admin/photos/upload",FakeHeaders(),data))
        
        status(result) must equalTo(CREATED)
        headers(result) must haveKeys(LOCATION)
        contentType(result) must beSome("application/json")      
        contentAsString(result) must /("success" -> "true")
    
      }
    
 
    }

  }