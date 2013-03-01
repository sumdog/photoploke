package models

case class Photo (
    data : Array[Byte],
    contentType: String,
    fileName : String
) 