package xyz.tg44.stringpic

import xyz.tg44.openscad.renderers.OpenScad.{saveFile, toSTL}
import xyz.tg44.openscad.viewers.MeshLab

import scala.concurrent.Future

object Example {



  def main(args: Array[String]): Unit = {
    import xyz.tg44.openscad.core.Renderable._
    import xyz.tg44.openscad.renderers.OpenScad._
    implicit val viewer: MeshLab = MeshLab()


    val path = "africa.png"
    val seed = 256
    val allEdges = BlackAndWhiteEdgeDetector.readImageEdges(path)
    val randomEdges = BlackAndWhiteEdgeDetector.randomFilter(0.2, allEdges, seed)

    val model = StringPicGenerator.generate(path, randomEdges, 1.2, 0.6, 10, 1, seed)
/*
    val model = StringPicGenerator.generateScadFromLayers(
      Seq(5->5, 10->10, 0->10, 10 -> 0),
      List(List((5->5, 10->10), (5->5, 0->10), (5->5, 10->0))), 0.8, 0.8, 1.0)

 */
    println("generated")
    saveFile(model, "test.scad")
    println("scad")
    toSTL(model, "test.stl")
    println("stl")
    //view(model)
    println("done")
  }
}
