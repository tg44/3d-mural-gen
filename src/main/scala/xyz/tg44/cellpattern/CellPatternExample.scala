package xyz.tg44.cellpattern

import xyz.tg44.cellpattern.parts.{CellPatternSettings, SingleLayer, SupportLayer, Tower}
import xyz.tg44.openscad.core.Solids.Union
import xyz.tg44.openscad.viewers.MeshLab

object CellPatternExample {
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters


  def main(args: Array[String]): Unit = {
    implicit val viewer: MeshLab = MeshLab()

    implicit val settings = CellPatternSettings(8, 0.2)

    val layersAsCells: Seq[Set[ConwayGOL.Cell]] = ConwayGOL.multipleEvolutions(25, ConwayGOL.methuselahs.rPentomino)

    val tower = Tower.generate(layersAsCells)

    view(tower)

    //val bridgeTestCubes = Union(BridgeTest.allPositioned.map{case (second, first) => Union(generateLayer(first), generateLayer(Set(second)).moveZ(settings.moveDistance))}: _*)
    //val bridgeTestLayers = Union(BridgeTest.allPositioned.map{case (second, first) => generateSupport(first, Set(second))}: _*)

    //val bridgeTestTest = Union(generateLayer(BridgeTest.threeEdge), generateLayer(Set(BridgeTest.middle)).moveZ(settings.moveDistance))
    //val bridgeTestTestLayers = generateSupport(BridgeTest.threeEdge, Set(BridgeTest.middle))


    //saveFile(Union(bridgeTestCubes, bridgeTestLayers), "test.scad")
    //toSTL(Union(bridgeTestCubes, bridgeTestLayers), "test.stl")
    //view(Union(bridgeTestCubes, bridgeTestLayers))
  }


}
