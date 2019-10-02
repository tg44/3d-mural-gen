package xyz.tg44.mural.consumer

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Comparator

import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import spray.json.{JsValue, JsonReader, RootJsonFormat}
import xyz.tg44.mural.parts.{Base, CuttingShape, MuralSettings, Top}
import xyz.tg44.mural.renderers.OpenScad
import xyz.tg44.mural.renderers.Renderable.RenderableForOps
import xyz.tg44.mural.renderers.Solids.{SurfaceFromFile, Union}
import xyz.tg44.mural.utils.AmpqHelper.CommitableWrapper
import xyz.tg44.mural.utils.{AmpqHelper, S3Uploader}

import scala.concurrent.{ExecutionContext, Future}

class MuralGenerator {
  import xyz.tg44.mural.consumer.MuralGenerator._

  import concurrent.ExecutionContext.Implicits._
  lazy val paralellism: Int = Runtime.getRuntime.availableProcessors()
  implicit val actorSystem = ActorSystem("mural-gen")
  implicit val materializer = ActorMaterializer()

  val flow = Flow[MuralMessage]
    .mapAsyncUnordered(paralellism)(createTheModel)
    .mapAsyncUnordered(paralellism*2)((uploadTheModel _).tupled)

  val stream = AmpqHelper.getConsumer("mural",flow)

}

object MuralGenerator {


  def model(msg: MuralMessage, img: File, workDir: Path): RenderableForOps = {
    import xyz.tg44.mural.renderers.OpenScad._
    import xyz.tg44.mural.renderers.Renderable._

    val settings = MuralSettings(msg.sideWidth, msg.maxXCoordinates, msg.maxYCoordinates, workDir)
    val cutter = CuttingShape.squareCutting(settings)
    val hexa = Union(
      Base.renderBase(msg.i, msg.j, settings),
      Top.renderTop(SurfaceFromFile(img), msg.i,msg.j, settings)
    )
    CuttingShape.makeCutting(cutter, hexa, msg.i, msg.j, settings)
  }

  def downloadImg(imageUrl: String, workDir: Path)(implicit executionContext: ExecutionContext): Future[File] = {
    import java.net.URL

    import sys.process._
    val file = File.createTempFile("baseImg", ".png", workDir.toFile)
    Future(new URL(imageUrl) #> file !!).map(_ => file)
  }

  def createTheModel(msg: MuralMessage)(implicit executionContext: ExecutionContext): Future[(MuralMessage, Path)] = {
    val workDir = Files.createTempDirectory("tmp-");
    for {
      img <- downloadImg(msg.imageUrl, workDir)
      model <- Future(model(msg, img, workDir))
      _ <- Future(OpenScad.toSTL(model, s"$workDir/out.stl", true))
    } yield {
      (msg, workDir)
    }
  }

  def uploadTheModel(msg: MuralMessage, workDir: Path)(implicit executionContext: ExecutionContext, materializer: Materializer) = {
    def deleteDirectory(dir: Path) = {
      Files.walk(dir)
        .sorted(Comparator.reverseOrder())
        .map[File](_.toFile)
        .forEach(_.delete);
    }
    val upload = S3Uploader.upload(workDir.resolve("out.stl"), s"${msg.outputPrefix}-${msg.i}-${msg.j}.stl")
    upload
      .flatMap(_ => Future(deleteDirectory(workDir)))
      .map(_ => msg)
  }

  case class MuralMessage(
    requestId: String,
    imageUrl: String,
    outputPrefix: String,
    sideWidth: Double,
    maxXCoordinates: Int,
    maxYCoordinates: Int,
    i: Int,
    j: Int,
    commitable: CommittableReadResult
  )

  case class MuralMessageData(
    requestId: String,
    imageUrl: String,
    outputPrefix: String,
    sideWidth: Double,
    maxXCoordinates: Int,
    maxYCoordinates: Int,
    i: Int,
    j: Int
  )

  import spray.json.DefaultJsonProtocol._
  implicit val MuralMessageDataSerialization: RootJsonFormat[MuralMessageData] = jsonFormat8(MuralMessageData)

  implicit val MuralMessageCommitable: CommitableWrapper[MuralMessage] = new CommitableWrapper[MuralMessage] {
    override def getCommittable(a: MuralMessage): CommittableReadResult = a.commitable

    override def reader(c: CommittableReadResult): JsonReader[MuralMessage] = {
      new JsonReader[MuralMessage]{
        def read(value: JsValue): MuralMessage = {
          (MuralMessage.apply _).tupled(MuralMessageData.unapply(value.convertTo[MuralMessageData]).get.append(c))
        }
      }
    }
  }

  implicit class TupOps8[A, B, C, D, E, F, G, H](val x: (A, B, C, D, E, F, G, H)) extends AnyVal {
    def append[I](y: I) = (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, y)
  }
}
