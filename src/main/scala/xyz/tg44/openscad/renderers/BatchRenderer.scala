package xyz.tg44.openscad.renderers

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.slf4j.LoggerFactory
import xyz.tg44.openscad.core.Renderable.RenderableForOps

import scala.concurrent.Future

class BatchRenderer(list: Seq[RenderableForOps], renderer: Renderer, paralellism: Int = 8, prefix: String = "") {
  import xyz.tg44.openscad.utils.Benchmark._

  def run = {
    implicit val system = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher
    val logger = LoggerFactory.getLogger("Benchmark")

    logger.info(s"pipeline started with ${list.size} element and $paralellism paralellism")

    new File(prefix).getParentFile.mkdirs;

    val stream = Source(list.toList)
      .zipWithIndex
      .mapAsyncUnordered(paralellism){case (m,i) => Future(renderer.toSTL(m, s"$prefix$i.stl").measure(s"stl generation on $i"))}
      .runWith(Sink.ignore)

    stream.onComplete(_ => system.terminate)

    stream
  }

}
