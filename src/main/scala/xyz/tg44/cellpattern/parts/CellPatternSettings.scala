package xyz.tg44.cellpattern.parts

case class CellPatternSettings(cubeSize: Int, layerHeight: Double, bigSupports: Boolean = false) {
  lazy val moveDistance = cubeSize-layerHeight
}
