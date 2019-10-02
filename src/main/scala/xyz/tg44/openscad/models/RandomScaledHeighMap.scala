package xyz.tg44.openscad.models

import scala.util.Random

object RandomScaledHeighMap {

  def apply(n: Int, maxHeight: Int, smoothing: Int, seed: Long): Seq[Seq[Double]] = {

    def avgVal(hm: Array[Array[Double]], i: Int, j: Int) = {
      val list = (for{
        a <- i-1 to i+1
        b <- j-1 to j+1
      } yield {
        if(0 <= a && a < n) {
          if(0 <= b && b < n) {
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
        0.0
      }
    }

    def avg(hms: Seq[Seq[Double]]): Seq[Seq[Double]] = {
      val hm = hms.map(_.toArray).toArray
      val list = for{
        i <- 0 until n
        j <- 0 until n
      } yield {
        avgVal(hm, i, j)
      }
      list.grouped(n).toSeq
    }

    val random = new Random(seed)
    val map: Seq[Seq[Double]] = (1 to n).map(_ => (1 to n).map(_ => random.nextDouble * maxHeight))

    val beautifyed = (1 to smoothing).foldLeft(map)( (a, b) => avg(a))

    val min = beautifyed.map(_.min).min
    val max = beautifyed.map(_.max).max
    val scale = maxHeight.toDouble/(max-min)
    val scaled = beautifyed.map(_.map(v => (v - min)*scale))

    scaled
  }

}
