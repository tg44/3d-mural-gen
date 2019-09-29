package xyz.tg44.mural.renderers

import java.io.File

import squants.{Angle, Length}
import xyz.tg44.mural.renderers.Primitives._
import xyz.tg44.mural.renderers.Renderable._


object Solids {

  case class Cube(width: Length, depth: Length, height: Length)
  case class Sphere(radius: Length)
  case class Cylinder(radiusBot: Length, radiusTop: Length, height: Length)
  object Cylinder {
    def apply(radius: Length, height: Length): Cylinder = Cylinder(radius, radius, height)
  }

  case class Empty()

  case class Polyhedron(faces: Iterable[Face]) {
    def indexed: (IndexedSeq[Point], Iterable[(Int, Int, Int)]) = Polyhedron.indexed(this)
  }
  object Polyhedron {

    def indexed(faces: Iterable[Face]): (IndexedSeq[Point], Iterable[(Int,Int,Int)]) = {
      val points = faces.foldLeft(Set[Point]())( (acc, face) => acc + face.p1 + face.p2 + face.p3 )
      val indexed = points.toIndexedSeq
      val idx: Map[Point, Int] = indexed.zipWithIndex.toMap
      (indexed, faces.map{ case Face(p1,p2,p3) => (idx(p1),idx(p2),idx(p3)) })
    }
    def indexed(p: Polyhedron): (IndexedSeq[Point], Iterable[(Int,Int,Int)]) = indexed(p.faces)
  }

  trait Extrudable[A]

  case class Extruded[A : Extrudable](twoD: A, height: Length, center: Boolean = false)(implicit val renderable: Renderable[A])
  case class Square(width: Length, height: Length)
  case class Text(text: String, textCentered: Boolean = false, size: Int = 10)
  case class Polygon(points: List[Point])

  case class Surface(heightMap: Seq[Seq[Double]])
  case class SurfaceFromFile(file: File)


  case class Union(objs: RenderableForOps*)
  case class Intersection(objs: RenderableForOps*)
  case class Difference(pos: RenderableForOps, negs: RenderableForOps*)

  case class Minkowski(objs: RenderableForOps*)
  case class Hull(objs: RenderableForOps*)

  case class Scale(x: Double, y: Double, z: Double, obj: RenderableForOps)
  case class Translate(x: Length, y: Length, z: Length, obj: RenderableForOps)

  object Translate {
    def apply(v: Vector, s: RenderableForOps): Translate = Translate(v.x, v.y, v.z, s)
  }

  case class Rotate(x: Angle, y: Angle, z: Angle, obj: RenderableForOps)

  object Rotate {
    def apply(q: Quaternion, s: RenderableForOps): Rotate = {
      // TODO make sure OpendSCAD use the same sequence of roation
      val v = q.toRollPitchYaw
      Rotate(v.x, v.y, v.z, s)
    }
  }

  case class Mirror(x: Double, y: Double, z: Double, obj: RenderableForOps)
  case class Multiply(m: Matrix, obj: RenderableForOps)

}
