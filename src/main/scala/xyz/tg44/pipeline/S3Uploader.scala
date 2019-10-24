package xyz.tg44.pipeline

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Comparator

import akka.stream.Materializer
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.FileIO
import org.slf4j.Logger
import xyz.tg44.pipeline.utils.Config.S3Config

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class S3Uploader(conf: S3Config) {
  def upload(file: Path, bucketKey: String)(implicit materializer: Materializer): Future[MultipartUploadResult] = {
    FileIO.fromPath(file).runWith(S3.multipartUpload(conf.bucket, bucketKey))
  }
}

object S3Uploader {
  def uploadModel(fileName: String, workDir: Path, logger: Logger)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader) = {
    def deleteDirectory(dir: Path) = {
      Files.walk(dir)
        .sorted(Comparator.reverseOrder())
        .map[File](_.toFile)
        .forEach(_.delete);
    }
    val upload = s3Helper.upload(workDir.resolve("out.stl"), fileName)
    val up = upload
      .flatMap(_ => Future(deleteDirectory(workDir)))
    up.onComplete{
      case Failure(exception) => logger.error(s"$fileName - upload - failed", exception)
      case _ => logger.info(s"$fileName - upload - ended")
    }
    up
  }

  def generateUrl(fileName: String) = "fileName"
}
