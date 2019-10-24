package xyz.tg44.pipeline.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.SourceQueueWithComplete
import spray.json.RootJsonFormat
import xyz.tg44.cellpattern.parts.CellPatternSettings
import xyz.tg44.mural.consumer.MuralGenerator
import xyz.tg44.mural.consumer.MuralGenerator.MuralJob
import xyz.tg44.pipeline.AmqpHelper.JobDto
import xyz.tg44.pipeline.server.MuralGeneratorApi.MuralJobDto
import xyz.tg44.pipeline.utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MuralGeneratorApi(pipeline: SourceQueueWithComplete[JobDto], idGenerator: IdGenerator)(implicit executionContext: ExecutionContext) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  def routes = {
    uploadMural ~ dummyJob
  }

  val uploadMural = (post & path("job" / "mural")) {
    entity(as[MuralJobDto]) { dto =>
      val (id, jobs) = generateJobsFromDto(dto)
      onComplete(Future.traverse(jobs)(pipeline.offer)) {
        case Success(_) => complete(id)
        case Failure(ex) =>
          ex.printStackTrace
          complete(StatusCodes.InternalServerError)
      }
    }
  }

  def generateJobsFromDto(dto: MuralJobDto): (String, Seq[JobDto]) = {
    val id = idGenerator()
    val jobs = for{
      i <- 0 to dto.maxXCoordinates
      j <- 0 to dto.maxYCoordinates
    } yield {
      new JobDto {
        override type A = MuralJob
        override val self = MuralJob(
          id,
          dto.imageUrl,
          dto.outputPrefix,
          dto.sideWidth,
          dto.maxXCoordinates,
          dto.maxYCoordinates,
          i,
          j
        )
        override val formatter = MuralGenerator.MuralJobFormat
      }
    }
    (id, jobs)
  }

  val dummyJob = (post & path("job" / "testMural")) {
    val dto = MuralJobDto(
      Some("test"),
      50,
      11,
      19,
      "https://github.com/tg44/3d-mural-gen/raw/master/N36W113.png",
      "grand-canyon",
    )
    val (id, jobs) = generateJobsFromDto(dto)
    import MuralGeneratorApi._
    onComplete(jobs.toFutureWithWait(pipeline.offer)) {
      case Success(_) => complete(id)
      case Failure(ex) =>
        ex.printStackTrace
        complete(StatusCodes.InternalServerError)
    }
  }
}

object MuralGeneratorApi {
  case class MuralJobDto(
    id: Option[String],
    sideWidth: Double,
    maxXCoordinates: Int,
    maxYCoordinates: Int,
    imageUrl: String,
    outputPrefix: String
  )

  import spray.json.DefaultJsonProtocol._
  implicit val MuralJobDtoFormat: RootJsonFormat[MuralJobDto] = jsonFormat6(MuralJobDto)

  implicit class SeqHelper[A](s: Seq[A]) {
    def toFutureWithWait[B](f: A => Future[B])(implicit executionContext: ExecutionContext): Future[Seq[B]] = {
      s.foldLeft(Future.successful(List.empty[B]))((fut, e) => fut.flatMap(fl => f(e).map(_ :: fl))).map(_.reverse)
    }
  }
}
