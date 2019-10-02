package xyz.tg44.openscad.core

import java.nio.file.Path

trait Renderable[A] {
  def render(s: A, indent: Int)(implicit context: Path): String
}

object Renderable {

  trait RenderableForOps {
    type INNER
    val a: INNER
    val fa: Renderable[INNER]

    override def toString: String = a.toString
    def render(indent: Int)(implicit context: Path): String = fa.render(a, indent)
    def renderWithHeader(implicit context: Path): String = {
      """$fa=4;
        |$fs=0.5;
        |""".stripMargin + render(0)
    }
  }

  object RenderableForOps {
    def apply[A](s: A)(implicit ev: Renderable[A]): RenderableForOps =
      new RenderableForOps {
        type INNER = A
        val a = s
        val fa = ev
      }
  }

  implicit def autoRenderableForOps[A](s: A)(implicit ev: Renderable[A]): RenderableForOps = RenderableForOps(s)
  implicit def autoRenderableSeqForOps[A](seq: Seq[A])(implicit ev: Renderable[A]): Seq[RenderableForOps] = seq.map(s => RenderableForOps(s))

  implicit final class RenderableForOpsHelper[A](private val lhs: A)(implicit renderer: Renderable[A]) {
    def toRenderableForOps: RenderableForOps = autoRenderableForOps(lhs)
  }
}
