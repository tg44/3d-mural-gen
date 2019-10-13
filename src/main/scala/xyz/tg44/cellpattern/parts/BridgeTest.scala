package xyz.tg44.cellpattern.parts

object BridgeTest {
  import cats.implicits._

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
