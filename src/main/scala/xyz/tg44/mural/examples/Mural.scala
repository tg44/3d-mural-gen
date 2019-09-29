package xyz.tg44.mural.examples

import java.io.File

import squants.space.Degrees
import xyz.tg44.mural.models.OpenLock
import xyz.tg44.mural.renderers.{BatchRenderer, OpenScad, Renderable}
import xyz.tg44.mural.renderers.Solids.{Union, _}
import xyz.tg44.mural.utils.PngHelper
import xyz.tg44.mural.viewers.MeshLab

import scala.concurrent.Future

object Mural {
  import xyz.tg44.mural.renderers.EverythingIsIn.millimeters
  import xyz.tg44.mural.renderers.InlineOps._
  import xyz.tg44.mural.renderers.Renderable._
  import xyz.tg44.mural.renderers.OpenScad._

  private val baseHeight = 8
  private val maxHeight = 257

  object Base {
    def renderBase(x: Int, y: Int, settings: BaseSettings) = {
      moveToHexaCoordinate(Difference(base(settings.sideWidth), captions(x, y, settings)), x, y, settings)
    }


    def base(sideWidth: Double): RenderableForOps = {
      val length = Math.sqrt(3)/2.0*sideWidth
      val c = Cube(length,sideWidth,baseHeight).move(-length, -sideWidth/2.0, 0)
      val hexa = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))) :_*)
      val clipcuts = Union((1 to 6).map(i => OpenLock.clipcut(0.2).moveX(length).rotateZ(Degrees(i * 60))) :_*)

      //val anchors = Union((1 to 6).map(i => Union(Hull(Cylinder(2, 3), Cylinder(2, 3).moveX(8)), Hull(Cylinder(4, 3), Cylinder(4, 3).moveX(8)).moveZ(3)).rotateZ(Degrees(i * 60))) :_*)

      val anchor = Union(Hull(Cylinder(3, 3.1), Cylinder(3, 3.1).moveY(8)), Hull(Cylinder(6, 3), Cylinder(6, 3).moveY(8)).moveZ(3))
      val anchorMiddle = Cylinder(6, 6)

      Difference(hexa, clipcuts, anchor, anchorMiddle)
    }

    def captions(x: Int, y: Int, settings: BaseSettings) = {
      val length = Math.sqrt(3)/2.0*settings.sideWidth
      val move = length /5 *3
      def getId(x: Int, y: Int): String = {
        s"$x,$y"
      }
      def idAsSolid(x: Int, y: Int) = Extruded(Text(getId(x, y), textCentered = true, size = 3), 0.8).mirror(1,0,0)

      def calcIdFromI(i: Int) = {
        //y = 1 => y-1; x, x+1, y; x-1 x+1 y+1; x, x+1
        //y = 2 => y-1; x-1, x, y; x-1 x+1 y+1; x-1, x

        val correction = if(y%2 == 0) -1 else 0

        i match {
          case 1 => idAsSolid(x+ 1 + correction, y -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case 2 => idAsSolid(x + correction, y -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case 3 => idAsSolid(x -1, y).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case 4 => idAsSolid(x + correction, y +1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case 5 => idAsSolid(x+ 1 + correction, y +1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case 6 => idAsSolid(x +1, y).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
          case _ => idAsSolid(-1, -1).rotateZ(Degrees(i * -60)).moveX(move).rotateZ(Degrees(i * 60))
        }
      }

      val captions = Union((1 to 6).map(i => calcIdFromI(i)) :_*)

      Union(captions, idAsSolid(x, y).moveY(length/5*2))
    }
  }


  object Top {
    def top(sideWidth: Double, hm: RenderableForOps): RenderableForOps = {
      val length = Math.sqrt(3) / 2.0 * sideWidth
      val c = Cube(length, sideWidth, sideWidth).move(-length, -sideWidth / 2.0, 0)
      val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)

      Intersection(hm, hexaValidVolume)
    }


    def topCutter(sideWidth: Double): RenderableForOps = {
      val length = Math.sqrt(3) / 2.0 * sideWidth
      val c = Cube(length, sideWidth, maxHeight).move(-length, -sideWidth / 2.0, 0)
      val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)
      hexaValidVolume
    }

    def renderTop(heightMap: RenderableForOps, x: Int, y: Int, settings: BaseSettings) = {
      heightMap.a match {
        case SurfaceFromFile(f) =>
          val smallerHm = fixupTopHm(f, x, y, settings)
          Intersection(smallerHm, moveToHexaCoordinate(topCutter(settings.sideWidth), x, y, settings)).moveZ(baseHeight)
        case _ =>
          Intersection(heightMap, moveToHexaCoordinate(topCutter(settings.sideWidth), x, y, settings)).moveZ(baseHeight)
      }
    }
  }

  def fixupTopHm(f: File, x: Int, y: Int, settings: BaseSettings) = {
    val marginOfError = 3
    val (xCoord, yCoord) = getXYCoordinates(x, y, settings)
    val tmpFile = java.io.File.createTempFile("fixed_hm", ".png")
    val w = settings.shortSide.toInt+1 + marginOfError*2
    val h = settings.longSide.toInt + 1 + marginOfError*2
    val smallerImg = PngHelper.cropImage(f, tmpFile, xCoord.toInt- w/2, yCoord.toInt - h/2, w+1, h + 1)
    SurfaceFromFile(smallerImg).move(xCoord.toInt-w/2, yCoord.toInt - h/2, 0)
  }

  object CuttingShape {

    val padding = 2

    def squareCutting(settings: BaseSettings) = {
      Square(settings.maxXPixels, settings.maxYPixels)
    }

    def intersectionShape[A: Extrudable : Renderable](cutter: A, settings: BaseSettings) = {
      val scaleX = (settings.maxXPixels - 2*padding).toDouble / settings.maxXPixels
      val scaleY =  (settings.maxYPixels - 2*padding).toDouble / settings.maxYPixels
      Extruded(cutter, maxHeight + baseHeight).scale(scaleX, scaleY, 1).move(padding, padding, 0)
    }

    def addedVolumeShape[A: Extrudable : Renderable](cutter: A, settings: BaseSettings) = {
      Difference(Extruded(cutter, baseHeight), intersectionShape(cutter, settings))
    }

    def validFinalVolume(sideWidth: Double): RenderableForOps = {
      val length = Math.sqrt(3) / 2.0 * sideWidth
      val c = Cube(length, sideWidth, maxHeight + baseHeight).move(-length, -sideWidth / 2.0, 0)
      val hexaValidVolume = Union((1 to 6).map(i => c.rotateZ(Degrees(i * 60))): _*)
      hexaValidVolume
    }

    def makeCutting[A: Extrudable : Renderable](cutter: A, hexa: RenderableForOps, x: Int, y: Int, settings: BaseSettings) = {
      Intersection(
        Union(
          addedVolumeShape(cutter, settings),
          Intersection(hexa, intersectionShape(cutter, settings))
        ),
        moveToHexaCoordinate(validFinalVolume(settings.sideWidth), x, y, settings)
      )
    }

  }

  def getXYCoordinates(x: Int, y: Int, settings: BaseSettings) = {
    val yMoveStep = Math.sqrt(3)*settings.shortSide / 2
    val xCoord = /*settings.shortSide/2 +*/ (if(y%2==0) 0 else settings.shortSide/2) + x * settings.shortSide
    val yCoord = settings.maxYPixels /*- settings.longSide/2*/ - y * yMoveStep
    (xCoord, yCoord)
  }

  def moveToHexaCoordinate(renderableForOps: RenderableForOps, x: Int, y: Int, settings: BaseSettings) = {
    val (xCoord, yCoord) = getXYCoordinates(x, y, settings)
    renderableForOps
      .moveX(xCoord)
      .moveY(yCoord)
  }

  def models[A: Extrudable : Renderable](cutter: A, heightMap: RenderableForOps, settings: BaseSettings) = {
    val tiles = for{
      i <- 0 to settings.maxXCoordinates
      j <- 0 to settings.maxYCoordinates
    } yield {
      val hexa = Union(
        Base.renderBase(i,j, settings),
        Top.renderTop(heightMap, i,j, settings)
      )
      CuttingShape.makeCutting(cutter, hexa, i, j, settings)
    }

    tiles
  }

  case class BaseSettings(
    sideWidth: Double,
    maxXCoordinates: Int,
    maxYCoordinates: Int
  ) {
    val longSide = 2*sideWidth //Y
    val shortSide = Math.sqrt(3)*sideWidth //X
    val oneLength = 2*sideWidth
    val maxYPixels = (longSide * (1 + (maxYCoordinates - 1) * 0.5)).toInt
    val maxXPixels = (shortSide * (maxXCoordinates + 0.5)).toInt
  }

  val magicCube = Difference(Cube(16,20,8), OpenLock.clipcut(0.2).move(16, 10, 0))

  def main(args: Array[String]): Unit = {
    import concurrent.duration._
    import xyz.tg44.mural.utils.Benchmark._
    import concurrent.Await
    import concurrent.ExecutionContext.Implicits.global
    implicit val viewer: MeshLab = MeshLab()

    //saveFile(models(50, 255), "test.scad")
    //saveFile(magicCube, "test.scad")
    //view(base(50))
    //view(Difference(base(50), captions(50, 100, 100, 50, 50)))
    //view(models(50, 255))

    //val settings = BaseSettings(50, 2, 2)
    val settings = BaseSettings(50, 11, 19)
    val seed = 255
    //val hm = Surface(RandomScaledHeighMap(Math.max(settings.maxXPixels, settings.maxYPixels), (settings.sideWidth/2).toInt, 5, seed))

    val lows = (settings.maxXPixels * settings.maxYPixels * 0.002).toInt
    val heights = (settings.maxXPixels * settings.maxYPixels * 0.002).toInt
    //val hm2 = Surface(FixedHeighhtMap(settings.maxYPixels, settings.maxXPixels, (settings.sideWidth/2).toInt, lows, heights, 12, 1, 1000, seed)).measure("hm generatrion")

    val hm3 = SurfaceFromFile(new File("N36W113.png"))
    //view(hm3)

    //saveFile(Union(hm3, fixupTopHm(hm3.file, 10, 10, settings), fixupTopHm(hm3.file, 1, 1, settings), Base.renderBase(1, 1, settings), fixupTopHm(hm3.file, 2, 7, settings)), "test.scad")

    //val hm = Empty()
    val cutter = CuttingShape.squareCutting(settings)

    //Await.result(Future.sequence(models(cutter, hm, settings).zipWithIndex.map{case (m,i) => Future(toSTL(m, s"test$i.stl"))}), 15.minutes)
    //saveFile(models(cutter, hm, settings).toList(12), "test.scad")
    //saveFile(models(cutter, hm, settings).toList(14), "test1.scad")
    //saveFile(models(cutter, hm, settings).toList(15), "test2.scad")
    //mergeFiles("test1.scad", "test2.scad", "test.scad")
    //toSTL(models(cutter, hm, settings).toList(14),"25.stl")
    //toSTL(models(cutter, hm, settings).toList(15),"26.stl")

    val model = models(cutter, hm3, settings).toList(42).measure("models")
    saveFile(model, "test.scad")
    Await.result(Future(toSTL(model, s"test.stl")), 60.minutes).measure("full generation pipeline")
    //val renderer = new BatchRenderer(models(cutter, hm3, settings), OpenScad, 8, "out/grand-canyon/fregment-")
    //Await.result(renderer.run, 60.minutes).measure("full generation pipeline")
  }

  def mergeFiles(f1: String, f2: String, fOut: String) = {
    import java.io._
    import scala.io.Source
    val part0 = Source.fromFile(f1).getLines
    val part1 = Source.fromFile(f2).getLines
    val part2 = part0.toList ++ part1.toList
    val part00002 = new File(fOut)
    val bw = new BufferedWriter(new FileWriter(part00002))
    part2.foreach(p => bw.write(p + "\n"))
    bw.close()
  }
}
