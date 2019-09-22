package xyz.tg44.mural.examples

import scala.util.Random

object FixedHeighhtMap {

  def apply(n: Int, m: Int, maxHeight: Int, numberOfMins: Int, numberOfMaxes: Int, weightsOfPoints: Int, avgingDistance: Int, smoothing: Int, seed: Long): Seq[Seq[Double]] = {
    val random = new Random(seed)

    val minPoints = (1 to numberOfMins).map(_ => random.nextInt(n) -> random.nextInt(m)).toSet
    val maxPoints = (1 to numberOfMaxes).map(_ => random.nextInt(n) -> random.nextInt(m)).toSet

    def avgVal(hm: Array[Array[Double]], i: Int, j: Int, weightsOfPoints: Int, minPoints: Set[(Int, Int)], maxPoints: Set[(Int, Int)]) = {
      if(maxPoints.contains(i -> j) || minPoints.contains(i -> j)){
        //println(i, j)
        hm(i)(j)
      } else {
        val list = (for {
          a <- i - avgingDistance to i + avgingDistance
          b <- j - avgingDistance to j + avgingDistance
        } yield {
          if (0 <= a && a < n) {
            if (0 <= b && b < m) {
              if(maxPoints.contains(a -> b) || minPoints.contains(a -> b)){
                (1 to weightsOfPoints).map(_ => hm(a)(b))
              } else {
                Seq(hm(a)(b))
              }
            } else {
              Seq()
            }
          } else {
            Seq()
          }
        }).flatten

        if (list.nonEmpty) {
          list.sum / list.size
        } else {
          println(i,j)
          0.0
        }
      }
    }

    def avg(hm: Array[Array[Double]], weightsOfPoints: Int, minPoints: Set[(Int, Int)], maxPoints: Set[(Int, Int)]): Array[Array[Double]] = {
      val list = for {
        i <- 0 until n
        j <- 0 until m
      } yield {
        avgVal(hm, i, j, weightsOfPoints, minPoints, maxPoints)
      }
      list.grouped(m).map(_.toArray).toArray
    }

    def setMinMax(hm: Seq[Seq[Double]]): Seq[Seq[Double]] = {
      val list = for {
        i <- 0 until n
        j <- 0 until m
      } yield {
        if(maxPoints.contains(i -> j)){
          maxHeight
        } else if(minPoints.contains(i -> j)){
          0
        } else {
          hm(i)(j)
        }
      }
      list.grouped(m).toSeq
    }

    val randomMap: Seq[Seq[Double]] = (1 to n).map(_ => (1 to m).map(_ => random.nextDouble * maxHeight))
    val map = setMinMax(randomMap).map(_.toArray).toArray
    val postProcessSteps = (smoothing * 0.1).toInt
    val beautifyed1 = (1 to smoothing-postProcessSteps).foldLeft(map)((a, b) => avg(a, weightsOfPoints, minPoints, maxPoints))
    val beautifyed2 = (1 to postProcessSteps).foldLeft(beautifyed1)((a, b) => avg(a, 1, minPoints, maxPoints))
    val beautifyed3 = avg(beautifyed2, 1, Set.empty, Set.empty)
    val beautifyed4 = avg(beautifyed3, 1, Set.empty, Set.empty)

    beautifyed4.map(_.toSeq).toSeq//.map(_.map(_.toInt))
  }


}
