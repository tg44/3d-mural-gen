package xyz.tg44.cellpattern.parts

import squants.space.Degrees
import xyz.tg44.openscad.core.Solids.{Difference, Empty, Intersection, Union}
import xyz.tg44.openscad.models.CenteredCube

object SupportLayer {
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import cats.implicits._

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

  def generate(prevCells: Set[Coordinate], livingCells: Set[Coordinate])(implicit settings: CellPatternSettings): RenderableForOps = {
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


}
