package xyz.tg44.pipeline

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.{AmqpSink, AmqpSource, CommittableReadResult}
import akka.stream.alpakka.amqp.{AmqpCredentials, AmqpDetailsConnectionProvider, AmqpUriConnectionProvider, AmqpWriteSettings, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorAttributes, OverflowStrategy, Supervision}
import akka.util.ByteString
import spray.json.{JsValue, JsonFormat}
import xyz.tg44.pipeline.AmqpHelper.JobDto
import xyz.tg44.pipeline.GeneratorFlow.Generator
import xyz.tg44.pipeline.utils.Config.AmqpConf

import scala.concurrent.Future
import scala.util.Try

class AmqpHelper(conf: AmqpConf) {

  def getConsumerGraph(queueName: String, parallelism: Int, generatorFlow: GeneratorFlow)(implicit actorSystem: ActorSystem): RunnableGraph[Future[Done]] = {
    import spray.json._

    val resumeOnParsingException = ActorAttributes.withSupervisionStrategy {
      new akka.japi.function.Function[Throwable, Supervision.Directive] {
        override def apply(t: Throwable): Supervision.Directive = t match {
          case _: spray.json.JsonParser.ParsingException => Supervision.Resume
          case _ => Supervision.stop
        }
      }
    }

    val queueDeclaration = QueueDeclaration(queueName)
    val connectionProvider = AmqpDetailsConnectionProvider(conf.host, conf.port).withCredentials(AmqpCredentials(conf.user, conf.pass))

    AmqpSource.committableSource(
      NamedQueueSourceSettings(connectionProvider, queueName)
        .withDeclaration(queueDeclaration),
      bufferSize = parallelism
    ).map { data =>
      (data.message.bytes.utf8String.parseJson, data)
    }.withAttributes(resumeOnParsingException)
      .via(generatorFlow.create())
      .mapAsync(1)(cm => cm.ack())
      .toMat(Sink.ignore)(Keep.right)
  }

  def getProducerGraph(queueName: String): RunnableGraph[SourceQueueWithComplete[JobDto]] = {
    val queueDeclaration = QueueDeclaration(queueName)
    val connectionProvider = AmqpDetailsConnectionProvider(conf.host, conf.port).withCredentials(AmqpCredentials(conf.user, conf.pass))

    val amqpSink: Sink[ByteString, Future[Done]] =
      AmqpSink.simple(
        AmqpWriteSettings(connectionProvider)
          .withRoutingKey(queueName)
          .withDeclaration(queueDeclaration)
      )

      Source.queue[JobDto](100, OverflowStrategy.backpressure)
        .map(d => d.formatter.write(d.self).toString)
        .map(s => ByteString(s))
        .to(amqpSink)
  }
}

object AmqpHelper {

  trait JobReader {
    type B
    val formatter: JsonFormat[B]
    val generator: Generator[B]

    def tryToParse(jsValue: JsValue, committable: CommittableReadResult): Option[JobDescription] = {
      Try{
        val data = jsValue.convertTo[B](formatter)
        val f = formatter
        val g = generator
        val c = committable
        new JobDescription{
          override type A = B
          override val self: A = data
          override val committable: CommittableReadResult = c
          override val formatter: JsonFormat[A] = f
          override val generator: Generator[A] = g
        }
      }.toOption
    }
  }

  trait JobDto {
    type A
    val self: A
    val formatter: JsonFormat[A]
  }

  trait JobDescription {
    type A
    val self: A
    val committable: CommittableReadResult
    val formatter: JsonFormat[A]
    val generator: Generator[A]
  }
}
