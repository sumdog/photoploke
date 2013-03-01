package util 

import play.api.mvc.BodyParsers.parse.Multipart.PartHandler
import play.api.mvc.BodyParsers.parse.Multipart.handleFilePart
import play.api.mvc.BodyParsers.parse.Multipart.FileInfo
import play.api.mvc.BodyParsers.parse.multipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.iteratee.Iteratee
import java.io.ByteArrayOutputStream
import play.api.mvc.BodyParser
import play.api.mvc.MultipartFormData

object CustomParsers {
 
  ///taken from http://stackoverflow.com/questions/15036121/pulling-files-from-multipartformdata-in-memory-in-play2-scala
  
  def multipartFormDataAsBytes:BodyParser[MultipartFormData[Array[Byte]]] = 
  multipartFormData(handleFilePartAsByteArray)
  
  def handleFilePartAsByteArray: PartHandler[FilePart[Array[Byte]]] =
  handleFilePart {
    case FileInfo(partName, filename, contentType) =>
      // simply write the data to the a ByteArrayOutputStream
      Iteratee.fold[Array[Byte], ByteArrayOutputStream](
        new ByteArrayOutputStream()) { (os, data) =>
          os.write(data)
          os
        }.mapDone { os =>
          os.close()
          os.toByteArray
        }
  }
}