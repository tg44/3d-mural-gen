package xyz.tg44.cellpattern

import java.nio.file.{Files, Path}

import akka.stream.Materializer
import org.slf4j.LoggerFactory
import spray.json.RootJsonFormat
import xyz.tg44.cellpattern.ConwayGOL.Cell
import xyz.tg44.cellpattern.parts.{CellPatternSettings, Coordinate, Tower}
import xyz.tg44.openscad.renderers.OpenScad
import xyz.tg44.pipeline.AmqpHelper.JobReader
import xyz.tg44.pipeline.GeneratorFlow.Generator
import xyz.tg44.pipeline.S3Uploader
import xyz.tg44.pipeline.utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}

object ConwayTowerGenerator {
  import xyz.tg44.openscad.utils.Benchmark._
  private val logger = LoggerFactory.getLogger("ConwayTowerGenerator")

  private def createModelF(msg: ConwayTowerJob)(workerContext: ExecutionContext): Future[Path] = {
    implicit val context = workerContext
    val workDir = Files.createTempDirectory("tmp-");
    for {
      model <- Future(model(msg, workDir)).measure(s"${msg.id.get} - model",logger)
      _ <- Future(OpenScad.toSTL(model, s"$workDir/out.stl", true)).measure(s"${msg.id.get} - stl",logger)
    } yield {
      workDir
    }
  }

  private def model(job: ConwayTowerJob, path: Path) = {
    implicit val settings = job.settings
    val layersAsCells: Seq[Set[ConwayGOL.Cell]] = ConwayGOL.multipleEvolutions(job.height, job.baseCells.toSet)
    Tower.generate(layersAsCells)
  }

  case class ConwayTowerJob(id: Option[String], settings: CellPatternSettings, height: Int, baseCells: Seq[Cell]) {
    def fixup(idGenerator: IdGenerator): ConwayTowerJob = copy(id = Option(idGenerator()))
  }

  lazy val ConwayTowerJobReader = new JobReader{
    override type B = ConwayTowerJob
    override val formatter: RootJsonFormat[ConwayTowerJob] = ConwayTowerJobFormat
    override val generator: Generator[ConwayTowerJob] = ConwayTowerGenerator
  }

  lazy val ConwayTowerGenerator = new Generator[ConwayTowerJob] {
    override def createModel(a: ConwayTowerJob)(workerContext: ExecutionContext): Future[Path] = createModelF(a)(workerContext)
    override def uploadModel(msg: ConwayTowerJob, workDir: Path)(implicit executionContext: ExecutionContext, materializer: Materializer, s3Helper: S3Uploader): Future[Unit] = {
      S3Uploader.uploadModel(s"${msg.id.get}.stl", workDir, logger)
      }.map(_ => ())
  }

  import spray.json.DefaultJsonProtocol._
  implicit lazy val CellFormat: RootJsonFormat[Cell] = jsonFormat2(Cell.apply)
  implicit lazy val CellPatternSettingsFormat: RootJsonFormat[CellPatternSettings] = jsonFormat3(CellPatternSettings)
  implicit lazy val ConwayTowerJobFormat: RootJsonFormat[ConwayTowerJob] = jsonFormat4(ConwayTowerJob)
}
