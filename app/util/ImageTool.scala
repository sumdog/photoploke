package util

import java.awt.{Color, RenderingHints, Image}
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import com.kitfox.svg.SVGUniverse
import java.io.{FileInputStream, ByteArrayInputStream, File}
import java.net.URI
import org.apache.batik.transcoder.image.{PNGTranscoder, ImageTranscoder}
import org.apache.batik.transcoder.{TranscodingHints, TranscoderInput, TranscoderOutput}

object WaterMarkLocation extends Enumeration {
  type WaterMarkLocation = Value
  val TOP_LEFT, TOP_CENTRE, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTRE, BOTTOM_RIGHT = Value
}

case class WaterMark(svg: Array[Byte], percentage : Double , location : WaterMarkLocation.Value, topMargin : Int, sideMargin:  Int)



object ImageTool {


  def processPhotos(original : BufferedImage, sizes : List[Int], watermark : Option[WaterMark] ) : Map[Int,BufferedImage] = {

    //add the original so we can make a watermarked version
    (sizes ::: List(original.getWidth)).foldLeft(Map[Int,BufferedImage]()) {
      (map, width) =>
        //only shrink, don't enlarge
        if (width <= original.getWidth) {
          map + {

            //only resize based on width and keep proportions
            val resized = if(width != original.getWidth) {
              Resizer.PROGRESSIVE_BILINEAR.resize(original,width,
                (width * original.getHeight)  / original.getWidth
              )
            }
            else {
              original
            }

            watermark match {
              case Some(w : WaterMark) => { width -> ImageTool.watermark(resized,w) }
              case None => { width -> resized }
            }

          }
        }
        else { map }
    }

  }


  def watermark(image : BufferedImage , watermark : WaterMark) : BufferedImage = {



    //load watermark with SVG Salamander because Batik can't get width/height,
    // and Salamander doesn't render SVGs correctly...seriously
    val svg = new SVGUniverse()
    val diagram = svg.getDiagram(svg.loadSVG(new ByteArrayInputStream(watermark.svg),"/watermark"))
    val svg_width = (image.getWidth() * watermark.percentage).toInt
    val svg_height = ((diagram.getHeight() * svg_width) / diagram.getWidth).toInt


    //Batik
    val svgR = new SVGRasterizer(new ByteArrayInputStream(watermark.svg))
    svgR.setImageWidth(svg_width)
    svgR.setImageHeight(svg_height)
    val mark = svgR.createBufferedImage()

    val composite = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB)
    val g = composite.getGraphics
    g.drawImage(image,0,0,null)

    val centerX = ((image.getWidth() / 2) - (svg_width / 2))
    val bottomY = image.getHeight() - svg_height - watermark.topMargin
    val rightX = image.getWidth() - svg_width - watermark.sideMargin

    watermark.location match {
      case WaterMarkLocation.TOP_LEFT =>      { g.drawImage(mark, watermark.sideMargin,  watermark.topMargin,null) }
      case WaterMarkLocation.TOP_CENTRE =>    { g.drawImage(mark, centerX,               watermark.topMargin,null) }
      case WaterMarkLocation.TOP_RIGHT =>     { g.drawImage(mark, rightX ,               watermark.topMargin,null) }
      case WaterMarkLocation.BOTTOM_LEFT =>   { g.drawImage(mark, watermark.sideMargin,  bottomY ,null ) }
      case WaterMarkLocation.BOTTOM_CENTRE => { g.drawImage(mark, centerX,               bottomY ,null ) }
      case WaterMarkLocation.BOTTOM_RIGHT =>  { g.drawImage(mark, rightX,                bottomY ,null ) }
    }

    g.dispose()

    //drop the alpha channel for JPEG compression
    val noAlpha = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB)
    val g2 = noAlpha.getGraphics
    g2.drawImage(composite,0,0,null)
    g2.dispose()

    noAlpha
  }
}