package xyz.tg44.pipeline

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.{AmqpSource, CommittableReadResult}
import akka.stream.alpakka.amqp.{AmqpUriConnectionProvider, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{ActorAttributes, Supervision}
import spray.json.JsonReader

object AmpqHelper {

  //todo url from config

  trait CommitableWrapper[A] {
    def getCommittable(a: A): CommittableReadResult
    def reader(c: CommittableReadResult): JsonReader[A]
  }

  def getConsumer[A](queueName: String, innerFlow: Flow[A, A, NotUsed])(implicit actorSystem: ActorSystem, ev: CommitableWrapper[A]) = {
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
    val connectionProvider = AmqpUriConnectionProvider(???)

    AmqpSource.committableSource(
      NamedQueueSourceSettings(connectionProvider, queueName)
        .withDeclaration(queueDeclaration),
      bufferSize = 2
    ).map { data =>
        val value = data.message.bytes.utf8String
        val sampleData = value.parseJson.convertTo[A](ev.reader(data))
        sampleData
      }.withAttributes(resumeOnParsingException)
      .via(innerFlow)
      .map(ev.getCommittable)
      .mapAsync(1)(cm => cm.ack())
      .to(Sink.ignore)
  }
}
