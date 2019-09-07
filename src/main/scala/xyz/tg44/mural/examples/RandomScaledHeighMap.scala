package xyz.tg44.mural.examples

import scala.util.Random

object RandomScaledHeighMap {

  def apply(n: Int, maxHeight: Int, smoothing: Int, seed: Long): Seq[Seq[Int]] = {

    def avgVal(hm: Seq[Seq[Int]], i: Int, j: Int) = {
      val list = (for{
        a <- i-1 to i+1
        b <- j-1 to j+1
      } yield {
        if(0 <= a && a < hm.size) {
          if(0 <= b && b < hm.size) {
            Some(hm(a)(b))
          } else {
            None
          }
        } else {
          None
        }
      }).collect{ case Some(n) => n}

      if(list.nonEmpty) {
        list.sum / list.size
      } else {
        0
      }
    }

    def avg(hm: Seq[Seq[Int]]): Seq[Seq[Int]] = {
      val list = for{
        i <- 0 until hm.size
        j <- 0 until hm.size
      } yield {
        avgVal(hm, i, j)
      }
      list.grouped(hm.size).toSeq
    }

    val random = new Random(seed)
    val map: Seq[Seq[Int]] = (1 to n).map(_ => (1 to n).map(_ => random.nextInt(maxHeight*smoothing)))

    val beautifyed = (1 to smoothing).foldLeft(map)( (a, b) => avg(a))

    val min = beautifyed.map(_.min).min
    val max = beautifyed.map(_.max).max
    val scale = maxHeight.toDouble/(max-min)
    val scaled = beautifyed.map(_.map(v => ((v - min)*scale).toInt))

    scaled
  }

}
