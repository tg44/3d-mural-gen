package xyz.tg44.mural.renderers

import java.io.{BufferedWriter, PrintWriter}

import squants.space.{Length, Millimeters}
import xyz.tg44.mural.renderers.Renderable.RenderableForOps
import xyz.tg44.mural.renderers.Solids._
import xyz.tg44.mural.utils.SysCmd
import xyz.tg44.mural.viewers.Viewer

object OpenScad extends Renderer {

  protected val command = "openscad"

  protected def length2Double(l: Length): Double = l to Millimeters

  implicit val emptyRenderer = new Renderable[Empty] {
    override def render(s: Empty, indent: Int): String = ""
  }

  implicit val cubeRenderer = new Renderable[Cube] {
    override def render(s: Cube, indent: Int): String = {
      (" "*indent) + "cube([ " + length2Double(s.width) + ", " + length2Double(s.depth) + ", " + length2Double(s.height) + "]);"+ "\n"
    }
  }

  implicit val sphereRenderer = new Renderable[Sphere] {
    override def render(s: Sphere, indent: Int): String = (" "*indent) + "sphere( " + length2Double(s.radius) + ");"+ "\n"
  }

  implicit val cylinderRenderer = new Renderable[Cylinder] {
    override def render(s: Cylinder, indent: Int): String = (" "*indent) + "cylinder( r1 = " + length2Double(s.radiusBot) + ", r2 = " + length2Double(s.radiusTop) + ", h = " + length2Double(s.height) + ");"+ "\n"
  }

  implicit val polyhedronRenderer = new Renderable[Polyhedron] {
    override def render(p: Polyhedron, indent: Int): String = {
      val (indexedP,indexedF) = p.indexed
      (" "*indent) +
        "polyhedron( points=[ " +
        indexedP.map(p => "["+ length2Double(p.x) +","+ length2Double(p.y) +","+ length2Double(p.z) +"]").mkString(", ") +
        " ], faces=[ " +
        indexedF.map{ case (a,b,c) => "["+a+","+b+","+c+"]" }.mkString(", ") +
        " ]);" + "\n"
    }
  }
/*
  implicit val fromFileRenderer = new Renderable[FromFile] {
    override def render(s: FromFile, indent: Int): String = {
      (" "*indent) + "import(\"" + s.path + "\");" + "\n"
    }
  }

 */

  implicit val unionRenderer = new Renderable[Union] {
    override def render(s: Union, indent: Int): String = {
      (" "*indent) + "union(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
      (" "*indent) +  "}" + "\n"
    }
  }
  implicit val intersectionRenderer = new Renderable[Intersection] {
    override def render(s: Intersection, indent: Int): String = {
      (" "*indent) + "intersection(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val differenceRenderer = new Renderable[Difference] {
    override def render(s: Difference, indent: Int): String = {
      (" "*indent) + "difference(){\n" +
        s.pos.render(indent+2) +
        s.negs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val minkowskiRenderer = new Renderable[Minkowski] {
    override def render(s: Minkowski, indent: Int): String = {
      (" "*indent) + "minkowski(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val hullRenderer = new Renderable[Hull] {
    override def render(s:  Hull, indent:  Int): String = {
      (" "*indent) + "hull(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }

  implicit val scaleRenderer = new Renderable[Scale] {
    override def render(s: Scale, indent: Int): String = {
      (" "*indent) + "scale(["+s.x+","+s.y+","+s.z+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val rotateRenderer = new Renderable[Rotate] {
    override def render(s: Rotate, indent: Int): String = {
      (" "*indent) + "rotate(["+s.x.toDegrees+","+s.y.toDegrees+","+s.z.toDegrees+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  implicit val translateRenderer = new Renderable[Translate] {
    override def render(s: Translate, indent: Int): String = {
      (" "*indent) + "translate(["+length2Double(s.x)+","+length2Double(s.y)+","+length2Double(s.z)+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val mirrorRenderer = new Renderable[Mirror] {
    override def render(s: Mirror, indent: Int): String = {
      (" "*indent) + "mirror(["+s.x+","+s.y+","+s.z+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val multiplyRenderer = new Renderable[Multiply] {
    override def render(s: Multiply, indent: Int): String = {
      (" "*indent) + "multmatrix([["+s.m.m00+","+s.m.m01+","+s.m.m02+","+s.m.m03+"],["+s.m.m10+","+s.m.m11+","+s.m.m12+","+s.m.m13+"],["+s.m.m20+","+s.m.m21+","+s.m.m22+","+s.m.m23+"],["+s.m.m30+","+s.m.m31+","+s.m.m32+","+s.m.m33+"],])" + "\n" +
        s.obj.render(indent + 2)
    }
  }

  implicit val polyRenderer = new Renderable[Polygon] {
    override def render(s: Polygon, indent: Int): String = {
        (" "*indent) +"polygon([ " + "\n" +
          (" "*(indent + 2)) + s.points.map(p => "[" + length2Double(p.x) + "," + length2Double(p.y) + "]").mkString(", ") + "\n" +
        (" "* indent) + "]);" + "\n"
    }
  }

  implicit val textRenderer = new Renderable[Text] {
    override def render(s: Text, indent: Int): String = {
        val centered = if (s.textCentered) """, halign="center", valign="center"""" else ""
        s"""${(" " * indent)}text("${s.text}"$centered, size=${s.size});
           |""".stripMargin
    }
  }

  implicit val squareRenderer = new Renderable[Square] {
    override def render(s: Square, indent: Int): String = {
      (" "*indent) + "square([" + length2Double(s.width) + "," + length2Double(s.height) + "]);" + "\n"
    }
  }

  implicit val polyExtrudable: Extrudable[Polygon] = new Extrudable[Polygon]{}
  implicit val textExtrudable: Extrudable[Text] = new Extrudable[Text]{}
  implicit val squareExtrudable: Extrudable[Square] = new Extrudable[Square]{}

  implicit def extrudedRenderer[A] = new Renderable[Extruded[A]] {
    override def render(s: Extruded[A], indent: Int): String = {
      (" "*indent) + "linear_extrude(" + length2Double(s.height) + ",center=" + (if (s.center) "true" else "false") + ")" + "\n" + s.renderable.render(s.twoD, indent+2)
    }
  }

  implicit val surfaceRenderer = new Renderable[Surface] {
    override def render(s: Surface, indent: Int): String = {
      val file = java.io.File.createTempFile("map", ".dat")
      val writer = new BufferedWriter(new PrintWriter(file))
      writer.write(s.heightMap.map(_.mkString(" ")).mkString("\n"))
      writer.close
      println(file.getAbsolutePath)
      (" "*indent) + "surface(file = \""+ file.getAbsolutePath +"\");" + "\n"
    }
  }

  implicit val surfacefromFileRenderer = new Renderable[SurfaceFromFile] {
    override def render(s: SurfaceFromFile, indent: Int): String = {
      (" "*indent) + "surface(file = \""+ s.file.getAbsolutePath +"\");" + "\n"
    }
  }

  protected def writeInFile(file: java.io.File, obj: RenderableForOps) = {
    val writer = new BufferedWriter(new PrintWriter(file))
    writer.write(obj.renderWithHeader)
    writer.close
  }

  protected def toTmpFile(obj: RenderableForOps) = {
    val tmpFile = java.io.File.createTempFile("model", ".scad")
    writeInFile(tmpFile, obj)
    tmpFile
  }

  def saveFile(obj: RenderableForOps, fileName: String) = {
    val file = new java.io.File(fileName)
    writeInFile(file, obj)
  }

  override def toSTL(obj: RenderableForOps, outputFile: String) = {
    import xyz.tg44.mural.utils.Benchmark._
    toSTL(obj, outputFile, Nil).measure(s"stl generation on $outputFile")
  }

  def toSTL(obj: RenderableForOps, outputFile: String, options: Iterable[String]) = {
    val tmpFile = toTmpFile(obj)
    val cmd = Array(command, tmpFile.getPath, "-o", outputFile) ++ options
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def view(obj: RenderableForOps, optionsRender: Iterable[String])(implicit viewer: Viewer): Unit = {
    import xyz.tg44.mural.utils.Benchmark._
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl").measure("scad generation")
    toSTL(obj, tmpFile.getPath, optionsRender).measure("stl generation")
    val res = viewer.view(tmpFile)
    tmpFile.delete
    res
  }

  def view(obj: RenderableForOps)(implicit viewer: Viewer): Unit = {
    view(obj, Nil)
  }
}
