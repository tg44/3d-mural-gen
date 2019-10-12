package xyz.tg44.cellpattern

import cats.Monoid
import squants.space.{Angle, Degrees}
import xyz.tg44.cellpattern.CellPatternExample.BridgeTest
import xyz.tg44.cellpattern.ConwayGOL.Cell
import xyz.tg44.openscad.core.Solids.{Difference, Empty, Intersection, Union}
import xyz.tg44.openscad.models.CenteredCube
import xyz.tg44.openscad.renderers.OpenScad
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

  case class CellPatternSettings(cubeSize: Int, layerHeight: Double, bigSupports: Boolean = false) {
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
  val topLeft = Coordinate(-1, 1)
  val topRight = Coordinate(1, 1)
  val bottomLeft = Coordinate(-1, -1)
  val bottomRight = Coordinate(1, -1)

  def generateSupport(prevCells: Set[Coordinate], livingCells: Set[Coordinate])(implicit settings: CellPatternSettings): RenderableForOps = {
    val cutting = settings.cubeSize-settings.layerHeight
    val cuttingSize = Math.sqrt(2*cutting*cutting)
    val halfCube = Difference(
      CenteredCube.xy(settings.cubeSize, settings.cubeSize, settings.cubeSize),
      CenteredCube(cuttingSize, cuttingSize, cuttingSize).rotateX(Degrees(45)).moveY(-settings.cubeSize/2.0)
    )
    val quarterCube = Difference(
      Intersection(halfCube, halfCube.rotateZ(Degrees(90))).rotateZ(Degrees(90))
    )
    def rotateToPositionDiagonal(shape: RenderableForOps,c: Coordinate, nb: Coordinate): RenderableForOps = {
      if((bottomRight |+| c) == nb) {
        shape.rotateZ(Degrees(90))
      } else if((bottomRight |+| c) == nb) {
        shape.rotateZ(Degrees(-90))
      } else if((topRight |+| c) == nb) {
        shape.rotateZ(Degrees(180))
      } else {
        shape
      }
    }
    def rotateToPositionSide(shape: RenderableForOps,c: Coordinate, nb: Coordinate): RenderableForOps = {
      if((left |+| c) == nb) {
        shape.rotateZ(Degrees(90))
      } else if((right |+| c) == nb) {
        shape.rotateZ(Degrees(-90))
      } else if((top |+| c) == nb) {
        shape
      } else {
        shape.rotateZ(Degrees(180))
      }
    }
    def generateCornerSupport(edges: Set[Coordinate], c: Coordinate) = {
      val negatedC = Coordinate(c.x * -1, c.y * -1)
      val choosenCorner = edges.combineWithEach(negatedC).reduce(_ |+| _) |+| c
      rotateToPositionDiagonal(quarterCube, c, choosenCorner)
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
              if(settings.bigSupports) {
                Union(prevEdges.map(e => rotateToPositionSide(halfCube, c, e)).toSeq: _*).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
              } else {
                generateCornerSupport(coveredVertices, c).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
              }
            } else if(verticesCoveredByEdges.size == 2) {
              Union(prevEdges.map(e => rotateToPositionSide(halfCube,c, e)).toSeq: _*).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
            } else {
              if(settings.bigSupports) {
                Union(prevDiagonals.map(d => rotateToPositionDiagonal(quarterCube, c, d)).toSeq: _*).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
              } else {
                generateCornerSupport(coveredVertices, c).translate(c.x * settings.moveDistance, c.y * settings.moveDistance, 0)
              }
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

    val layersAsCells = ConwayGOL.multipleEvolutions(25, ConwayGOL.methuselahs.rPentomino)
    val layerPairs = layersAsCells.zip(layersAsCells.tail)
    val tower = Union(layersAsCells.zipWithIndex.map { case (cells, i) => generateLayer(cells).moveZ(settings.moveDistance * i) }: _*)
    val supports = Union(layerPairs.zipWithIndex.map{case ((first, second), i) => generateSupport(first, second).moveZ(settings.moveDistance * i)}: _*)

    view(Union(tower, supports))

    //val bridgeTestCubes = Union(BridgeTest.allPositioned.map{case (second, first) => Union(generateLayer(first), generateLayer(Set(second)).moveZ(settings.moveDistance))}: _*)
    //val bridgeTestLayers = Union(BridgeTest.allPositioned.map{case (second, first) => generateSupport(first, Set(second))}: _*)

    //val bridgeTestTest = Union(generateLayer(BridgeTest.threeEdge), generateLayer(Set(BridgeTest.middle)).moveZ(settings.moveDistance))
    //val bridgeTestTestLayers = generateSupport(BridgeTest.threeEdge, Set(BridgeTest.middle))


    //saveFile(Union(bridgeTestCubes, bridgeTestLayers), "test.scad")
    //toSTL(Union(bridgeTestCubes, bridgeTestLayers), "test.stl")
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
    val threeEdge = Set(Coordinate(-1, -1), Coordinate(1, -1), Coordinate(1, 1))

    val all = Seq(
      under,
      fourVertecies,
      edge,
      twoEdgeV1,
      twoEdgeV2,
      edgeAndVertex,
      edgeAnd2Vertex,
      twoEdgeAndVertex,
      threeEdge
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
