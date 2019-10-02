package xyz.tg44.cellpattern

import cats.Monoid
import squants.space.{Angle, Degrees}
import xyz.tg44.cellpattern.CellPatternExample.BridgeTest
import xyz.tg44.cellpattern.ConwayGOL.Cell
import xyz.tg44.openscad.core.Solids.{Difference, Empty, Union}
import xyz.tg44.openscad.models.CenteredCube
import xyz.tg44.openscad.viewers.MeshLab

object CellPatternExample {

  import cats.implicits._
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters

  /* TODOs:
   * get the max base in settings, cut out cubes
   * handle the 3 diagonal case
   * probably would be nice top support too for better bridging
   */

  case class Coordinate(x: Int, y: Int)

  case class CellPatternSettings(cubeSize: Int, layerHeight: Double) {
    lazy val moveDistance = cubeSize-layerHeight
  }

  def generateLayer(livingCells: Set[Coordinate])(implicit settings: CellPatternSettings): RenderableForOps = {
    Union(livingCells.map(c => CenteredCube.xy(settings.cubeSize, settings.cubeSize, settings.cubeSize).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)).toSeq: _*)
  }

  val edgeCoords = (for {
    x <- - 1 to 1
    y <- - 1 to 1
    if(x == 0 || y ==0)
  } yield Coordinate(x, y)).toSet

  val diagonalCoords = (for {
    x <- - 1 to 1
    y <- - 1 to 1
    if(x != 0 && y !=0 )
  } yield Coordinate(x, y)).toSet
  val left = Coordinate(-1, 0)
  val right = Coordinate(1, 0)
  val top = Coordinate(0, 1)
  val bottom = Coordinate(0, -1)

  def generateSupport(prevCells: Set[Coordinate], livingCells: Set[Coordinate])(implicit settings: CellPatternSettings): RenderableForOps = {
    val cuttingSize = Math.sqrt(2*settings.cubeSize*settings.cubeSize)
    val halfCube = Difference(
      CenteredCube.xy(settings.cubeSize, settings.cubeSize, settings.cubeSize),
      CenteredCube(cuttingSize, cuttingSize, cuttingSize).rotateX(Degrees(45)).moveY(-settings.cubeSize/2.0)
    )
    def rotateHalfCube(c: Coordinate, nb: Coordinate): RenderableForOps = {
      if((left |+| c) == nb) {
        halfCube.rotateZ(Degrees(90))
      } else if((right |+| c) == nb) {
        halfCube.rotateZ(Degrees(-90))
      } else if((top |+| c) == nb) {
        halfCube
      } else {
        halfCube.rotateZ(Degrees(180))
      }
    }

    Union(
      livingCells.map[RenderableForOps, Set[RenderableForOps]] { c =>
        if (prevCells.contains(c)) {
          Empty()
        } else {
          val diagonals = diagonalCoords.combineWithEach(c)
          val prevEdges = edgeCoords.combineWithEach(c) intersect prevCells
          val prevDiagonals = diagonals intersect prevCells
          val verticesCoveredByEdges = prevEdges.flatMap(edgeCoords.combineWithEach) intersect diagonals
          val coveredVertices = verticesCoveredByEdges ++ prevDiagonals
          if(coveredVertices.size == 4) {
            Empty()
          } else {
            //the cases are separated for later adjustment options
            if(verticesCoveredByEdges.size == 3) {
              Union(prevEdges.map(e => rotateHalfCube(c, e)).toSeq: _*).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
            } else if(verticesCoveredByEdges.size == 2) {
              Union(prevEdges.map(e => rotateHalfCube(c, e)).toSeq: _*).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
            } else {
              throw new Exception(s"can't support this layout; edges: $prevEdges diagonals: $prevDiagonals")
            }
          }
        }
      }.toSeq : _*
    )
  }

  def main(args: Array[String]): Unit = {
    import xyz.tg44.openscad.utils.Benchmark._
    import concurrent.Await
    import concurrent.ExecutionContext.Implicits.global
    import concurrent.duration._
    implicit val viewer: MeshLab = MeshLab()

    implicit val settings = CellPatternSettings(8, 0.2)

    val layersAsCells = ConwayGOL.evolveMultiple(12, ConwayGOL.methuselahs.rPentomino).zipWithIndex
    val tower = Union(layersAsCells.map { case (cells, i) => generateLayer(cells).moveZ(settings.moveDistance * i) }: _*)

    //view(tower)

    val bridgeTestCubes = Union(BridgeTest.allPositioned.map{case (second, first) => Union(generateLayer(first), generateLayer(Set(second)).moveZ(settings.moveDistance))}: _*)
    val bridgeTestLayers = Union(BridgeTest.allPositioned.map{case (second, first) => generateSupport(first, Set(second))}: _*)

    saveFile(Union(bridgeTestCubes, bridgeTestLayers), "test.scad")
    //view(Union(bridgeTestCubes, bridgeTestLayers))
  }

  implicit def cellSetToCoordinateSet(s: Set[Cell]): Set[Coordinate] = {
    s.map(c => Coordinate(c.x, c.y))
  }

  implicit val CoordinateMonoid: Monoid[Coordinate] = new Monoid[Coordinate] {
    override def empty: Coordinate = Coordinate(0, 0)

    override def combine(a: Coordinate, b: Coordinate): Coordinate = Coordinate(a.x + b.x, a.y + b.y)
  }

  implicit class SetAddHelper[A : Monoid](s: Set[A]) {
    def combineWithEach(a: A): Set[A] = s.map(_ |+| a)
  }

  object BridgeTest {
    val middle = Coordinate(0, 0)

    val under = Set(Coordinate(0, 0))
    val fourVertecies = Set(Coordinate(-1, -1), Coordinate(1, -1), Coordinate(1, 1), Coordinate(-1, 1))
    val edge = Set(Coordinate(-1, 0))
    val twoEdgeV1 = Set(Coordinate(-1, 0), Coordinate(1, 0))
    val twoEdgeV2 = Set(Coordinate(-1, 0), Coordinate(0, -1))
    val edgeAndVertex = Set(Coordinate(-1, 0), Coordinate(1, 1))
    val edgeAnd2Vertex = Set(Coordinate(-1, 0), Coordinate(1, 1), Coordinate(1, -1))
    val twoEdgeAndVertex = Set(Coordinate(-1, 0), Coordinate(0, -1), Coordinate(1, 1))

    val all = Seq(
      under,
      fourVertecies,
      edge,
      twoEdgeV1,
      twoEdgeV2,
      edgeAndVertex,
      edgeAnd2Vertex,
      twoEdgeAndVertex
    )

    val allPositioned = all
      .grouped(3).zipWithIndex
      .flatMap{case (g, i) =>
        g.zipWithIndex.map{case (s, j) =>
          val translate = Coordinate(i*4, j*4)
          (middle |+| translate) -> s.combineWithEach(translate)
        }
      }.toList
  }

}
