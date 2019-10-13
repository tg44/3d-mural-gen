package xyz.tg44.cellpattern.parts

import xyz.tg44.cellpattern.ConwayGOL
import xyz.tg44.openscad.core.Solids.Union

object Tower {
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import cats.implicits._

  def generate(layersAsCells: Seq[Set[ConwayGOL.Cell]])(implicit settings: CellPatternSettings): RenderableForOps = {
    val layerPairs = layersAsCells.zip(layersAsCells.tail)
    val tower = Union(layersAsCells.zipWithIndex.map { case (cells, i) => SingleLayer.generate(cells).moveZ(settings.moveDistance * i) }: _*)
    val supports = Union(layerPairs.zipWithIndex.map { case ((first, second), i) => SupportLayer.generate(first, second).moveZ(settings.moveDistance * i) }: _*)

    Union(tower, supports)
  }
}
