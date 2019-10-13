package xyz.tg44.cellpattern.parts

import cats.Monoid
import xyz.tg44.cellpattern.ConwayGOL.Cell

case class Coordinate(x: Int, y: Int)

object Coordinate {
  import cats.implicits._

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
}
