package xyz.tg44.cellpattern.parts

import xyz.tg44.openscad.core.Solids.Union
import xyz.tg44.openscad.models.CenteredCube

object SingleLayer {
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters

  def generate(livingCells: Set[Coordinate])(implicit settings: CellPatternSettings): RenderableForOps = {
    Union(livingCells.map(c => CenteredCube.xy(settings.cubeSize, settings.cubeSize, settings.cubeSize).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)).toSeq: _*)
  }

}
