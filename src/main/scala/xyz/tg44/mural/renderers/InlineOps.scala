package xyz.tg44.mural.renderers

import squants.space.Degrees
import squants.{Angle, Length}
import xyz.tg44.mural.renderers.Renderable._
import xyz.tg44.mural.renderers.Solids._
import xyz.tg44.mural.renderers.Primitives._

object InlineOps {
  object any2stringadd

  implicit final class AngleConversions[A](n: A)(implicit num: Numeric[A]) {
    def ° = Degrees(n)
  }

  implicit def renderableOps(a: RenderableForOps): Ops[a.INNER] = {
    implicit val fa = a.fa
    a.a
  }

  implicit final class Ops[A](val lhs: A)(implicit renderer: Renderable[A]) {

    import squants.space.LengthConversions._

    def translate(x: Length, y: Length, z: Length) = Translate(x, y, z, lhs)

    def move(x: Length, y: Length, z: Length) = Translate(x, y, z, lhs)

    def move(v: Vector) = Translate(v, lhs)

    def move(p: Point) = Translate(p.toVector, lhs)

    def moveX(x: Length) = Translate(x, 0 mm, 0 mm, lhs)

    def moveY(y: Length) = Translate(0 mm, y, 0 mm, lhs)

    def moveZ(z: Length) = Translate(0 mm, 0 mm, z, lhs)

    def rotate(x: Angle, y: Angle, z: Angle) = Rotate(x, y, z, lhs)

    def rotate(q: Quaternion) = Rotate(q, lhs)

    def rotateX(x: Angle) = Rotate(x, 0 °, 0 °, lhs)

    def rotateY(y: Angle) = Rotate(0 °, y, 0 °, lhs)

    def rotateZ(z: Angle) = Rotate(0 °, 0 °, z, lhs)

    def scale(x: Double, y: Double, z: Double) = Scale(x, y, z, lhs)

    def scaleX(x: Double) = Scale(x, 1, 1, lhs)

    def scaleY(y: Double) = Scale(1, y, 1, lhs)

    def scaleZ(z: Double) = Scale(1, 1, z, lhs)

    def mirror(x: Double, y: Double, z: Double) = Mirror(x, y, z, lhs)

    def multiply(m: Matrix) = Multiply(m, lhs)

    def +[B](rhs: RenderableForOps) = Union(lhs, rhs)

    def ++(rhs: Iterable[RenderableForOps]) = Union((lhs.toRenderableForOps :: rhs.toList): _*)

    def union[B](rhs: RenderableForOps) = Union(lhs, rhs)

    def *[B](rhs: RenderableForOps) = Intersection(lhs, rhs)

    def **(rhs: Iterable[RenderableForOps]) = Intersection((lhs.toRenderableForOps :: rhs.toList): _*)

    def intersection[B](rhs: RenderableForOps) = Intersection(lhs, rhs)

    def -[B](rhs: RenderableForOps) = Difference(lhs, rhs)

    def --(rhs: Iterable[RenderableForOps]) = Difference(lhs, rhs.toList: _*)

    def difference[B](rhs: RenderableForOps) = Difference(lhs, rhs)

    def hull[B](rhs: RenderableForOps) = Hull(lhs, rhs)

    def minkowski[B](rhs: RenderableForOps) = Minkowski(lhs, rhs)

  }

}
