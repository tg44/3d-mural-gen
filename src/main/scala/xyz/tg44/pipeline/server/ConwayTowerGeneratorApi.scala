package xyz.tg44.pipeline.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.SourceQueueWithComplete
import xyz.tg44.cellpattern.{ConwayGOL, ConwayTowerGenerator}
import xyz.tg44.cellpattern.ConwayTowerGenerator.ConwayTowerJob
import xyz.tg44.cellpattern.parts.CellPatternSettings
import xyz.tg44.pipeline.AmqpHelper.JobDto
import xyz.tg44.pipeline.utils.IdGenerator

import scala.util.{Failure, Success}

class ConwayTowerGeneratorApi(pipeline: SourceQueueWithComplete[JobDto], idGenerator: IdGenerator) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  def routes = {
    uploadConway ~ dummyJob
  }

  val uploadConway = (post & path("job" / "conway")) {
    entity(as[ConwayTowerJob]) { dto =>
      val jobDto = new JobDto {
        override type A = ConwayTowerJob
        override val self = dto.fixup(idGenerator)
        override val formatter = ConwayTowerGenerator.ConwayTowerJobFormat
      }
      onComplete(pipeline.offer(jobDto)) {
        case Success(_)  => complete(jobDto.self)
        case Failure(_) =>
          complete(StatusCodes.InternalServerError)
      }
    }
  }
  val dummyJob = (post & path("job" / "testConway")) {
    val jobDto = new JobDto {
      override type A = ConwayTowerJob
      override val self = ConwayTowerJob(Some("test"), CellPatternSettings(8, 0.2, false), 25, ConwayGOL.methuselahs.rPentomino.toSeq)
      override val formatter = ConwayTowerGenerator.ConwayTowerJobFormat
    }
    onComplete(pipeline.offer(jobDto)) {
      case Success(_)  => complete(jobDto.self)
      case Failure(ex) =>
        ex.printStackTrace
        complete(StatusCodes.InternalServerError)
    }
  }
}

