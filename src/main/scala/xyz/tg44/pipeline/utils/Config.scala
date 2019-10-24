package xyz.tg44.pipeline.utils

import pureconfig.generic.ProductHint
import xyz.tg44.pipeline.utils.Config.{AmqpConf, S3Config}

class Config {
  import pureconfig._
  import pureconfig.generic.auto._
  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  val amqp =ConfigSource.default.at("amqp").loadOrThrow[AmqpConf]
  val s3 = ConfigSource.default.at("s3").loadOrThrow[S3Config]
}

object Config {

  case class AmqpConf(url: String)
  case class S3Config(bucket: String)

}
