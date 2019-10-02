package xyz.tg44.mural.parts

import java.io.File

import squants.space.Degrees
import xyz.tg44.mural.renderers.Solids.{Cube, Intersection, SurfaceFromFile, Union}
import xyz.tg44.mural.utils.PngHelper

object Top extends CoordinateHelper {
  import xyz.tg44.mural.renderers.EverythingIsIn.millimeters
  import xyz.tg44.mural.renderers.InlineOps._
  import xyz.tg44.mural.renderers.OpenScad._
  import xyz.tg44.mural.renderers.Renderable._

  def top(sideWidth: Double, hm: RenderableForOps): RenderableForOps = {
    val length = Math.sqrt(3) / 2.0 * sideWidth
    val c = Cube(length, sideWidth, sideWidth).move(-length, -sideWidth / 2.0, 0)
    val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)

    Intersection(hm, hexaValidVolume)
  }

  def topCutter(sideWidth: Double, settings: MuralSettings): RenderableForOps = {
    val length = Math.sqrt(3) / 2.0 * sideWidth
    val c = Cube(length, sideWidth, settings.maxHeight).move(-length, -sideWidth / 2.0, 0)
    val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)
    hexaValidVolume
  }

  def renderTop(heightMap: RenderableForOps, x: Int, y: Int, settings: MuralSettings) = {
    heightMap.a match {
      case SurfaceFromFile(f) =>
        val smallerHm = fixupTopHm(f, x, y, settings)
        Intersection(smallerHm, moveToHexaCoordinate(topCutter(settings.sideWidth, settings), x, y, settings)).moveZ(settings.baseHeight)
      case _ =>
        Intersection(heightMap, moveToHexaCoordinate(topCutter(settings.sideWidth, settings), x, y, settings)).moveZ(settings.baseHeight)
    }
  }

  def fixupTopHm(f: File, x: Int, y: Int, settings: MuralSettings) = {
    val marginOfError = 3
    val (xCoord, yCoord) = getXYCoordinates(x, y, settings)
    val tmpFile = File.createTempFile("fixed_hm", ".png", settings.basePath.toFile)
    val w = settings.shortSide.toInt+1 + marginOfError*2
    val h = settings.longSide.toInt + 1 + marginOfError*2
    val smallerImg = PngHelper.cropImage(f, tmpFile, xCoord.toInt- w/2, yCoord.toInt - h/2, w+1, h + 1)
    SurfaceFromFile(smallerImg).move(xCoord.toInt-w/2, yCoord.toInt - h/2, 0)
  }
}
