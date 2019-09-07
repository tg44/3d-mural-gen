package xyz.tg44.mural.renderers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import xyz.tg44.mural.renderers.Renderable.RenderableForOps

import scala.concurrent.Future

class BatchRenderer(list: Seq[RenderableForOps], renderer: Renderer, paralellism: Int = 8) {

  def run = {
    implicit val system = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val stream = Source(list.toList)
      .zipWithIndex
      .mapAsyncUnordered(paralellism){case (m,i) => Future(renderer.toSTL(m, s"test$i.stl"))}
      .runWith(Sink.ignore)

    stream.onComplete(_ => system.terminate)

    stream
  }

}
