package xyz.tg44.pipeline

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.slf4j.LoggerFactory
import xyz.tg44.cellpattern.ConwayTowerGenerator
import xyz.tg44.mural.consumer.MuralGenerator
import xyz.tg44.pipeline.server.{AkkaWebserver, ConwayTowerGeneratorApi, MuralGeneratorApi}
import xyz.tg44.pipeline.utils.{Config, IdGenerator, LogBridge}

import scala.concurrent.ExecutionContext
import scala.util.Failure

object GeneratorPipelineMain extends App {
  LogBridge.initLogBridge()

  lazy val parallelism: Int = Runtime.getRuntime.availableProcessors()

  private val logger = LoggerFactory.getLogger("Main")
  logger.info(s"Current parallelism is: $parallelism")

  implicit val actorSystem: ActorSystem = ActorSystem("model-gen")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  val workerEc = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(parallelism * 2))

  val config = new Config()
  val amqpHelper = new AmqpHelper(config.amqp)
  implicit val s3Uploader: S3Uploader = new S3Uploader(config.s3)

  val idGenerator: IdGenerator = new IdGenerator{}

  val jobReaders = Seq(
    ConwayTowerGenerator.ConwayTowerJobReader,
    MuralGenerator.MuralJobReader
  )
  val generatorFlow = new GeneratorFlow(parallelism, jobReaders, workerEc)

  val producerPipeline = amqpHelper.getProducerGraph("models").run()
  val workerPipeline = amqpHelper.getConsumerGraph("models", parallelism, generatorFlow).run()

  val conwayTowerGeneratorApi = new ConwayTowerGeneratorApi(producerPipeline, idGenerator)
  val muralGeneratorApi = new MuralGeneratorApi(producerPipeline, idGenerator)

  val binding = AkkaWebserver.startWebserver(conwayTowerGeneratorApi.routes, muralGeneratorApi.routes)

  workerPipeline.onComplete{
    case Failure(exception) => exception.printStackTrace
    case _ => println("completed workPipeline")
  }
}
