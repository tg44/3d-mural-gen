package xyz.tg44.mural.parts

import xyz.tg44.openscad.core.Renderable
import xyz.tg44.openscad.core.Solids.Translate

trait CoordinateHelper {
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._

  def getXYCoordinates(x: Int, y: Int, settings: MuralSettings) = {
    val yMoveStep = Math.sqrt(3)*settings.shortSide / 2
    val xCoord = /*settings.shortSide/2 +*/ (if(y%2==0) 0 else settings.shortSide/2) + x * settings.shortSide
    val yCoord = settings.maxYPixels /*- settings.longSide/2*/ - y * yMoveStep
    (xCoord, yCoord)
  }

  def moveToHexaCoordinate(renderableForOps: RenderableForOps, x: Int, y: Int, settings: MuralSettings)(implicit ev: Renderable[Translate]) = {
    val (xCoord, yCoord) = getXYCoordinates(x, y, settings)
    renderableForOps
      .moveX(xCoord)
      .moveY(yCoord)
  }

}
