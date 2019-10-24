package xyz.tg44.openscad.utils

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

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

  implicit class FutureHelper[A](f: Future[A]) {
    def measure(s: String, logger: Logger)(implicit executionContext: ExecutionContext): Future[A] = {
      for {
        start <- Future.successful(System.nanoTime)
        _ = logger.info(s"$s - started")
        res <- f
      } yield {
        val end = System.nanoTime
        logger.info(s"$s - ended: ${(end-start)/(1000.0*1000.0)}ms")
        res
      }
    }
  }
}
