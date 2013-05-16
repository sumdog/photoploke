package models

import play.api.data.Forms._


case class PhotoInformation(name: String,
    description: String,
    tags: Array[String],
    albums: Array[String]) {

  def apply(test : String) {}
}