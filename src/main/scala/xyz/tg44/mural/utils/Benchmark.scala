package xyz.tg44.mural.utils

import org.slf4j.LoggerFactory

object Benchmark {

  val logger = LoggerFactory.getLogger("Benchmark")

  implicit class FunctionHelper[A](f: => A) {
    def measure(s: String): A = {
      val start = System.nanoTime
      val a = f
      val end = System.nanoTime
      logger.info(s"$s - ${(end-start)/1000.0/1000.0} ms")
      a
    }
  }
}
