package xyz.tg44.openscad.renderers

import xyz.tg44.openscad.core.Renderable.RenderableForOps

trait Renderer {
  def toSTL(obj: RenderableForOps, outputFile: String): Unit
}
