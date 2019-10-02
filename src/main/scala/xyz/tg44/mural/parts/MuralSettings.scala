package xyz.tg44.mural.parts

import java.nio.file.Path

case class MuralSettings(
  sideWidth: Double,
  maxXCoordinates: Int,
  maxYCoordinates: Int,
  basePath: Path
) {
  val longSide = 2*sideWidth //Y
  val shortSide = Math.sqrt(3)*sideWidth //X
  val oneLength = 2*sideWidth
  val maxXPixels = (shortSide * (maxXCoordinates + 0.5)).toInt
  val maxYPixels = (longSide * (1 + (maxYCoordinates - 1) * 0.5)).toInt
  val baseHeight = 8
  val maxHeight = 257
}
