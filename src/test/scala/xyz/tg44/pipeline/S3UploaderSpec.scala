package xyz.tg44.pipeline

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.WordSpecLike
import xyz.tg44.pipeline.utils.Config.S3Config

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class S3UploaderSpec extends WordSpecLike {

  def await[T](future: Future[T]): T = {
    Await.result(future, 30.seconds)
  }

  implicit val as = ActorSystem("test")
  implicit val materializer = ActorMaterializer()

  "uploader" should {

    "upload" in {
      val u = new S3Uploader(S3Config("test"))
      await(u.upload(Paths.get("N36W113.png"), "N36W113.png"))
    }

  }

}
