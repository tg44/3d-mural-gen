package xyz.tg44.mural.examples

import java.io.File
import java.nio.file.Paths

import xyz.tg44.mural.parts.{Base, CuttingShape, MuralSettings, Top}
import xyz.tg44.openscad.core.Renderable
import xyz.tg44.openscad.core.Solids.{Union, _}
import xyz.tg44.openscad.models.OpenLock
import xyz.tg44.openscad.viewers.MeshLab

import scala.concurrent.Future

object MuralExample {
  import xyz.tg44.openscad.core.InlineOps._
  import xyz.tg44.openscad.core.Renderable._
  import xyz.tg44.openscad.renderers.OpenScad._
  import xyz.tg44.openscad.utils.EverythingIsIn.millimeters

  def models[A: Extrudable : Renderable](cutter: A, heightMap: RenderableForOps, settings: MuralSettings) = {
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

  val magicCube = Difference(Cube(16,20,8), OpenLock.clipcut(0.2).move(16, 10, 0))

  def main(args: Array[String]): Unit = {
    import xyz.tg44.openscad.utils.Benchmark._

    import concurrent.Await
    import concurrent.ExecutionContext.Implicits.global
    import concurrent.duration._
    implicit val viewer: MeshLab = MeshLab()

    //saveFile(models(50, 255), "test.scad")
    //saveFile(magicCube, "test.scad")
    //view(base(50))
    //view(Difference(base(50), captions(50, 100, 100, 50, 50)))
    //view(models(50, 255))

    //val settings = BaseSettings(50, 2, 2)
    val settings = MuralSettings(50, 11, 19, Paths.get("."))
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
    Await.result(Future(toSTL(model, s"test.stl").measure(s"stl generation")), 60.minutes).measure("full generation pipeline")
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
