package xyz.tg44.mural.parts

import squants.space.Degrees
import xyz.tg44.openscad.models.OpenLock
import xyz.tg44.openscad.core.Solids._

object Base extends CoordinateHelper {
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.core.Renderable._

  def renderBase(x: Int, y: Int, settings: MuralSettings) = {
    moveToHexaCoordinate(Difference(base(settings.sideWidth, settings), captions(x, y, settings)), x, y, settings)
  }

  def base(sideWidth: Double, settings: MuralSettings): RenderableForOps = {
    val length = Math.sqrt(3)/2.0*sideWidth
    val c = Cube(length,sideWidth, settings.baseHeight).move(-length, -sideWidth/2.0, 0)
    val hexa = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))) :_*)
    val clipcuts = Union((1 to 6).map(i => OpenLock.clipcut(0.2).moveX(length).rotateZ(Degrees(i * 60))) :_*)

    //val anchors = Union((1 to 6).map(i => Union(Hull(Cylinder(2, 3), Cylinder(2, 3).moveX(8)), Hull(Cylinder(4, 3), Cylinder(4, 3).moveX(8)).moveZ(3)).rotateZ(Degrees(i * 60))) :_*)

    val anchor = Union(Hull(Cylinder(3, 3.1), Cylinder(3, 3.1).moveY(8)), Hull(Cylinder(6, 3), Cylinder(6, 3).moveY(8)).moveZ(3))
    val anchorMiddle = Cylinder(6, 6)

    Difference(hexa, clipcuts, anchor, anchorMiddle)
  }

  def captions(x: Int, y: Int, settings: MuralSettings) = {
    val length = Math.sqrt(3)/2.0*settings.sideWidth
    val move = length /5 *3
    def getId(x: Int, y: Int): String = {
      s"$x,$y"
    }
    def idAsSolid(x: Int, y: Int) = Extruded(Text(getId(x, y), textCentered = true, size = 3), 0.8).mirror(1,0,0)

    def calcIdFromI(i: Int) = {
      //y = 1 => y-1; x, x+1, y; x-1 x+1 y+1; x, x+1
      //y = 2 => y-1; x-1, x, y; x-1 x+1 y+1; x-1, x

      val correction = if(y%2 == 0) -1 else 0

      i match {
        case 1 => idAsSolid(x+ 1 + correction, y -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case 2 => idAsSolid(x + correction, y -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case 3 => idAsSolid(x -1, y).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case 4 => idAsSolid(x + correction, y +1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case 5 => idAsSolid(x+ 1 + correction, y +1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case 6 => idAsSolid(x +1, y).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        case _ => idAsSolid(-1, -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
      }
    }

    val captions = Union((1 to 6).map(i => calcIdFromI(i)) :_*)

    Union(captions, idAsSolid(x, y).moveY(length/5*2))
  }
}
