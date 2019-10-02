package xyz.tg44.openscad.models

import xyz.tg44.openscad.core.Primitives.Point
import xyz.tg44.openscad.core.Renderable._
import xyz.tg44.openscad.core.InlineOps._
import xyz.tg44.openscad.core.Renderable
import xyz.tg44.openscad.core.Solids._
import xyz.tg44.openscad.utils.EverythingIsIn.millimeters

//rewritten based on https://github.com/caitlynb/OpenSCAD-OpenLock/blob/master/OpenLock.scad
// its pozitioned to 0 at Z, 0 to negative direction by X and middle by Y
// good to differentiate from a `translate([-16,-10,0]) cube([16,20,8]);`
object OpenLock {

  private val cutoutheight = 4.2
  private val cutoutstartz = 1.4
  private val cutoutwide1 = 14
  private val cutoutdeep1 = 2
  private val cutoutdeep2 = 2
  private val cutoutwide2 = 12
  private val cutoutwide3 = 10
  private val cutoutdeep3 = 5

  def clipsupport(layerheight: Double)(implicit ev1: Renderable[Cube], ev2: Renderable[Translate], ev3: Renderable[Union], ev4: Renderable[Difference]): RenderableForOps = {
    Difference(
      CenteredCube(cutoutdeep3+1, 6, cutoutheight-layerheight*2),
      CenteredCube(cutoutdeep3-1, 4, cutoutheight+layerheight*2)
    ).move(-(cutoutdeep3+1)/2.0, 0, cutoutstartz+layerheight+(cutoutheight-layerheight*2)/2.0)
  }

  def clipcut(layerheight: Double)(implicit ev1: Renderable[Cube], ev2: Renderable[Translate], ev3: Renderable[Union], ev4: Renderable[Rotate], ev5: Renderable[Polygon], ev6: Renderable[Extruded[Polygon]], ev7: Extrudable[Polygon]): RenderableForOps = {
    Union(
      Extruded(
        Polygon(
          Point(-cutoutwide1 / 2.0, -2, 0) ::
            Point(-cutoutwide1 / 2.0, cutoutdeep1, 0) ::
            Point(-cutoutwide2 / 2.0, cutoutdeep2, 0) ::
            Point(-cutoutwide3 / 2.0, cutoutdeep3, 0) ::
            Point(-cutoutwide3 / 2.0, cutoutdeep3 + 2, 0) ::
            Point(cutoutwide3 / 2.0, cutoutdeep3 + 2, 0) ::
            Point(cutoutwide3 / 2.0, cutoutdeep3, 0) ::
            Point(cutoutwide2 / 2.0, cutoutdeep2, 0) ::
            Point(cutoutwide1 / 2.0, cutoutdeep1, 0) ::
            Point(cutoutwide1 / 2, -2, 0) ::
            Nil
        ),
        cutoutheight,
        center = true
      ).rotateZ(90°)
       .move(0,0,cutoutheight/2.0+cutoutstartz),

      CenteredCube(4.7,18,cutoutheight+cutoutstartz+2).move(-8.35,0,(cutoutheight+cutoutstartz)/2.0-1),
      CenteredCube(4.7,14,cutoutheight+cutoutstartz+layerheight).move(-8.35,0,(cutoutheight+cutoutstartz+layerheight*2)/2.0)
    )
  }

}
