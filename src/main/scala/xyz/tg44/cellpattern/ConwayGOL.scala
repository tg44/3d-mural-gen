package xyz.tg44.cellpattern

import scala.annotation.tailrec

object ConwayGOL {

  case class Cell(x: Int, y: Int)

  def evolve(cells: Set[Cell]): Set[Cell] = {
    candidateCells(cells).filter { candidate =>
      val n = liveNeighbours(candidate, cells).size
      (cells.contains(candidate) && n == 2) || n == 3
    }
  }

  def multipleEvolutions(n: Int, cells: Set[Cell]): List[Set[Cell]] = {
    @tailrec
    def rec(n: Int, acc: List[Set[Cell]]): List[Set[Cell]] = {
      if(n>0) {
        val e = evolve(acc.head)
        rec(n-1, e :: acc)
      } else {
        acc
      }
    }

    rec(n, cells :: Nil).reverse
  }

  private def candidateCells(cells: Set[Cell]): Set[Cell] = {
    cells.flatMap { cell =>
      for {
        x <- cell.x - 1 to cell.x + 1
        y <- cell.y - 1 to cell.y + 1
      } yield Cell(x, y)
    }
  }

  private def liveNeighbours(candidate: Cell, cells: Set[Cell]): Set[Cell] = {
    cells.filter { cell =>
      cell != candidate &&
        math.abs(cell.x - candidate.x) <= 1 &&
        math.abs(cell.y - candidate.y) <= 1
    }
  }

  object oscillators {
    val blinker = Set(Cell(0,-1), Cell(0,0), Cell(0,1))
  }

  object methuselahs {
    val rPentomino = Set(Cell(0,0), Cell(0,1), Cell(0,-1), Cell(1,1), Cell(-1,0))
  }
}
