package xyz.tg44.pipeline

import java.nio.file.Path

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
import akka.stream.scaladsl.Flow
import spray.json.{JsValue, JsonFormat}
import xyz.tg44.pipeline.AmqpHelper.{JobDescription, JobReader}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class GeneratorFlow(parallelism: Int, jobReaders: Seq[JobReader], workerContext: ExecutionContext)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader) {
  def create(): Flow[(JsValue, CommittableReadResult), CommittableReadResult, NotUsed] = {
    Flow[(JsValue, CommittableReadResult)]
      .map{ case (jsValue, committable) =>
        jobReaders.foldLeft(Option.empty[JobDescription])((acc, g) =>
          acc.orElse(
            g.tryToParse(jsValue, committable)
          )
        )
      }
      .collect{case Some(d) => d}
      .mapAsyncUnordered(parallelism){ g =>
        g.generator.createModel(g.self)(workerContext).map(g -> _)
      }
      .mapAsyncUnordered(parallelism*2){case (g, p) => g.generator.uploadModel(g.self, p).map(_ => g)}
      .map{g => g.committable}
  }
}

object GeneratorFlow {
  trait Generator[A] {
    def createModel(a: A)(workerContext: ExecutionContext): Future[Path]
    def uploadModel(msg: A, workDir: Path)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader): Future[Unit]
  }
}
