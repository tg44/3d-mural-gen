package xyz.tg44.stringpic

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import squants.space.{Degrees, Radians}
import xyz.tg44.openscad.core.Solids.{Cube, Cylinder, Union}

import scala.util.Random

object StringPicGenerator {
  type Point = (Int, Int)
  type Line = (Point, Point)

  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.core.Renderable._

  def generate(path: String, edges: Seq[Point], lineWidth: Double, lineHeight: Double, levels: Int, scale: Double, seed: Long): RenderableForOps = {
    val random = new Random(seed)
    val imageFile = new File(path)
    val img = ImageIO.read(imageFile)
    val grayImage = new BufferedImage(img.getWidth(), img.getHeight(),
      BufferedImage.TYPE_BYTE_BINARY);
    val g = grayImage.createGraphics
    g.drawImage(img, 0, 0, null)

    @scala.annotation.tailrec
    def rec(remLevels: Int, layers: List[List[Line]], priorityPoints: List[Point]): List[List[Line]] = {
      if(remLevels == 0) {
        layers
      } else {
        val nonPrioEdges = edges.filter(e => !priorityPoints.contains(e))
        val rEdges = (random.shuffle(priorityPoints) ++ random.shuffle(nonPrioEdges))
        val newLayer = generateLayer(layers.flatten, grayImage, rEdges)
        rec(remLevels -1, newLayer._1 :: layers, newLayer._2)
      }
    }

    val calculatedLayers = rec(levels, Nil, Nil)
    generateScadFromLayers(edges, calculatedLayers, lineWidth, lineHeight, scale)
  }

  def generateScadFromLayers(edges: Seq[Point], layers: List[List[Line]], lineWidth: Double, lineHeight: Double, scale: Double): RenderableForOps = {
    def edge(e: Point): RenderableForOps = Cylinder(lineWidth/2.0, lineHeight * layers.size.toDouble).move(e._1 * scale, e._2 * scale, 0.0)
    def line(level: Int, l: Line): RenderableForOps = {
      val length = Math.sqrt((l._1._1 - l._2._1)*(l._1._1 - l._2._1) + (l._1._2 - l._2._2)*(l._1._2 - l._2._2))*scale
      val rotate = Math.acos((l._1._1 - l._2._1)*scale/length) * (if(l._2._2 > l._1._2) -1 else 1)
      Cube(length, lineWidth, lineHeight).moveY(-lineWidth/2.0).moveX(-length/2.0).rotateZ(Radians(rotate)).move((l._1._1+l._2._1)/2.0 * scale, (l._1._2 + l._2._2)/2.0 * scale, lineHeight*level)
    }
    Union(
      Union(edges.map(edge): _*),
      Union(layers.zipWithIndex.flatMap(z => z._1.map( l => line(z._2, l))): _*)
    ).rotateX(Degrees(180))
  }

  def generateLayer(noGoLines: Seq[Line], image: BufferedImage, edges: Seq[Point]) = {
    require(edges.nonEmpty)

    @scala.annotation.tailrec
    def rec(notUsedPoints: List[Point], usedPoints: List[Point], lines: List[Line], notFound: List[Point]): (List[Line], List[Point]) = {
      notUsedPoints match {
        case h :: t =>
          val noGoPoints = noGoLines.filter(l => l._1 == h || l._2 == h).map(l => if(l._1 == h) l._2 else l._1)
          val p = usedPoints.filter(p => !noGoPoints.contains(p)).filter(p => checkNoLineCut(lines, h, p)).find(p => checkConvexity(image, h, p))
          if(p.isDefined) {
            rec(t, h :: usedPoints, (h, p.get) :: lines, notFound)
          } else {
            rec(t, h :: usedPoints, lines, h :: notFound)
          }
        case Nil =>
          (lines, notFound)
      }
    }

    rec(edges.toList.tail, edges.head :: Nil, Nil, Nil)
  }

  //true if from->to line has the same color
  def checkConvexity(image: BufferedImage, from: Point, to: Point): Boolean = {
    val steps = Math.max(Math.abs(from._1 - to._1), Math.abs(from._2 - to._2))
    val xs = (to._1 - from._1) / steps.toDouble
    val ys = (to._2 - from._2) / steps.toDouble
    (for (i <- 1 until steps) yield {
      val prevPixel = image.getRGB((from._1 + (i - 1) * xs).toInt, (from._2 + (i - 1) * ys).toInt)
      val thisPixel = image.getRGB((from._1 + i * xs).toInt, (from._2 + i * ys).toInt)
      prevPixel == thisPixel
    }).forall(identity)
  }

  //true if they intersect
  def checkLineCut(l1: (Point, Point), l2: (Point, Point)): Boolean = {
    //https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
    def onSegment(p: Point, q: Point, r: Point) = {
      q._1 <= Math.max(p._1, r._1) && q._1 >= Math.min(p._1, r._1) &&
        q._2 <= Math.max(p._2, r._2) && q._2 >= Math.min(p._2, r._2)
    }
    def orientation(p: Point, q: Point, r: Point) = {
      // See https://www.geeksforgeeks.org/orientation-3-ordered-points/
      val or = (q._2 - p._2) * (r._1 - q._1) - (q._1 - p._1) * (r._2 - q._2)
      if (or == 0) {
        0
      } else if (or > 0) {
        1
      } else {
        2
      }
    }

      // Find the four orientations needed for general and
      // special cases
      val o1 = orientation(l1._1, l1._2, l2._1);
      val o2 = orientation(l1._1, l1._2, l2._2);
      val o3 = orientation(l2._1, l2._2, l1._1);
      val o4 = orientation(l2._1, l2._2, l1._2);

      // General case
      if (o1 != o2 && o3 != o4) {
        true
      } else {
        // Special Cases
        if (o1 == 0 && onSegment(l1._1, l2._1, l1._2)) {
          // p1, q1 and p2 are colinear and p2 lies on segment p1q1
          true
        } else if (o2 == 0 && onSegment(l1._1, l2._2, l1._2)) {
          // p1, q1 and q2 are colinear and q2 lies on segment p1q1
          true
        } else if (o3 == 0 && onSegment(l2._1, l1._1, l2._2)) {
          // p2, q2 and p1 are colinear and p1 lies on segment p2q2
          true
        } else if (o4 == 0 && onSegment(l2._1, l1._2, l2._2)) {
          // p2, q2 and q1 are colinear and q1 lies on segment p2q2
          true
        } else {
          // Doesn't fall in any of the above cases
          false
        }
      }
  }

  def checkNoLineCut(set: Seq[(Point, Point)], from: Point, to: Point): Boolean = {
    set.forall(l => !checkLineCut(l, (from, to)))
  }

}
