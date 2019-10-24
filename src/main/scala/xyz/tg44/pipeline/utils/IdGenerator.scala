package xyz.tg44.pipeline.utils

import java.util.UUID

trait IdGenerator {

  def apply(): String = UUID.randomUUID.toString

}
