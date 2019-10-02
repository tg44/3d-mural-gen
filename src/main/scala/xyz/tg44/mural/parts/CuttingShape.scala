package xyz.tg44.mural.parts

import squants.space.Degrees
import xyz.tg44.openscad.core.Renderable
import xyz.tg44.openscad.core.Solids._

object CuttingShape extends CoordinateHelper {
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.core.Renderable._

  val padding = 2

  def squareCutting(settings: MuralSettings) = {
    Square(settings.maxXPixels, settings.maxYPixels)
  }

  def intersectionShape[A: Extrudable : Renderable](cutter: A, settings: MuralSettings) = {
    val scaleX = (settings.maxXPixels - 2*padding).toDouble / settings.maxXPixels
    val scaleY =  (settings.maxYPixels - 2*padding).toDouble / settings.maxYPixels
    Extruded(cutter, settings.maxHeight + settings.baseHeight).scale(scaleX, scaleY, 1).move(padding, padding, 0)
  }

  def addedVolumeShape[A: Extrudable : Renderable](cutter: A, settings: MuralSettings) = {
    Difference(Extruded(cutter, settings.baseHeight), intersectionShape(cutter, settings))
  }

  def validFinalVolume(sideWidth: Double, settings: MuralSettings): RenderableForOps = {
    val length = Math.sqrt(3) / 2.0 * sideWidth
    val c = Cube(length, sideWidth, settings.maxHeight + settings.baseHeight).move(-length, -sideWidth / 2.0, 0)
    val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)
    hexaValidVolume
  }

  def makeCutting[A: Extrudable : Renderable](cutter: A, hexa: RenderableForOps, x: Int, y: Int, settings: MuralSettings) = {
    Intersection(
      Union(
        addedVolumeShape(cutter, settings),
        Intersection(hexa, intersectionShape(cutter, settings))
      ),
      moveToHexaCoordinate(validFinalVolume(settings.sideWidth, settings), x, y, settings)
    )
  }

}
