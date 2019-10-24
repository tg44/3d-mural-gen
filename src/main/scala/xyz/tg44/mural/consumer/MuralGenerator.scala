package xyz.tg44.mural.consumer

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Comparator

import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.CommittableReadResult
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import org.slf4j.LoggerFactory
import spray.json.{JsValue, JsonReader, RootJsonFormat}
import xyz.tg44.cellpattern.ConwayTowerGenerator.logger
import xyz.tg44.mural.parts.{Base, CuttingShape, MuralSettings, Top}
import xyz.tg44.openscad.renderers.OpenScad
import xyz.tg44.openscad.core.Renderable.RenderableForOps
import xyz.tg44.openscad.core.Solids.{SurfaceFromFile, Union}
import xyz.tg44.pipeline.AmqpHelper.JobReader
import xyz.tg44.pipeline.GeneratorFlow.Generator
import xyz.tg44.pipeline.{AmqpHelper, S3Uploader}

import scala.concurrent.{ExecutionContext, Future}

object MuralGenerator {
  private val logger = LoggerFactory.getLogger("MuralGenerator")
  import xyz.tg44.openscad.utils.Benchmark._

  private def model(msg: MuralJob, img: File, workDir: Path): RenderableForOps = {
    import xyz.tg44.openscad.renderers.OpenScad._
    import xyz.tg44.openscad.core.Renderable._

    val settings = MuralSettings(msg.sideWidth, msg.maxXCoordinates, msg.maxYCoordinates, workDir)
    val cutter = CuttingShape.squareCutting(settings)
    val hexa = Union(
      Base.renderBase(msg.i, msg.j, settings),
      Top.renderTop(SurfaceFromFile(img), msg.i,msg.j, settings)
    )
    CuttingShape.makeCutting(cutter, hexa, msg.i, msg.j, settings)
  }

  private def downloadImg(imageUrl: String, workDir: Path)(implicit executionContext: ExecutionContext): Future[File] = {
    import java.net.URL

    import sys.process._
    val file = File.createTempFile("baseImg", ".png", workDir.toFile)
    Future(new URL(imageUrl) #> file !!).map(_ => file)
  }

  private def createTheModel(msg: MuralJob)(implicit executionContext: ExecutionContext): Future[Path] = {
    val workDir = Files.createTempDirectory("tmp-");
    for {
      img <- downloadImg(msg.imageUrl, workDir)
      model <- Future(model(msg, img, workDir)).measure(s"${msg.requestId} ${msg.i},${msg.j} - model",logger)
      _ <- Future(OpenScad.toSTL(model, s"$workDir/out.stl", true)).measure(s"${msg.requestId} ${msg.i},${msg.j} - stl",logger)
    } yield {
      workDir
    }
  }

  private def uploadTheModel(msg: MuralJob, workDir: Path)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader) = {
    S3Uploader.uploadModel(s"${msg.requestId}/${msg.outputPrefix}-${msg.i}-${msg.j}.stl", workDir, logger)
  }

  case class MuralJob(
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
  implicit val MuralJobFormat: RootJsonFormat[MuralJob] = jsonFormat8(MuralJob)

  lazy val MuralJobReader = new JobReader{
    override type B = MuralJob
    override val formatter: RootJsonFormat[MuralJob] = MuralJobFormat
    override val generator: Generator[MuralJob] = MuralGenerator
  }

  lazy val MuralGenerator = new Generator[MuralJob] {
    override def createModel(a: MuralJob)(workerContext: ExecutionContext): Future[Path] = createTheModel(a)(workerContext)
    override def uploadModel(msg: MuralJob, workDir: Path)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader): Future[Unit] = {
      uploadTheModel(msg, workDir)
      }.map(_ => ())
  }
}
