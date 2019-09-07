package xyz.tg44.mural.renderers

import xyz.tg44.mural.renderers.Renderable.RenderableForOps

trait Renderer {
  def toSTL(obj: RenderableForOps, outputFile: String): Unit
}
