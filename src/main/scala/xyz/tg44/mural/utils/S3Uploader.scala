package xyz.tg44.mural.utils

import java.nio.file.Path

import akka.stream.Materializer
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.FileIO

import scala.concurrent.Future

object S3Uploader {

  //TODO bucket from config

  def upload(file: Path, bucketKey: String)(implicit materializer: Materializer): Future[MultipartUploadResult] = {
    FileIO.fromPath(file).runWith(S3.multipartUpload(???, bucketKey))
  }
}
