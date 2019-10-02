package xyz.tg44.openscad.utils

import java.io.File

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.PngWriter

object PngHelper {

  def cropImage(inFile: File, outputFile: File, x: Int, y: Int, w: Int, h: Int): File = {
    implicit val writer = PngWriter.NoCompression
    val img = Image.fromFile(inFile)
    val wOrigin = img.width
    val hOrigin = img.height
    val image = img.trim(x,hOrigin-(y+h), wOrigin-(x+w), y)
    image.output(outputFile)
  }
}
