package xyz.tg44.stringpic

import java.io.File

import scala.util.Random

object BlackAndWhiteEdgeDetector {

  def readImageEdges(path: String): Seq[(Int, Int)] = {
    import javax.imageio.ImageIO
    import java.awt.image.BufferedImage
    val imageFile = new File(path)
    val img = ImageIO.read(imageFile)
    val grayImage = new BufferedImage(img.getWidth(), img.getHeight(),
      BufferedImage.TYPE_BYTE_BINARY);
    val g = grayImage.createGraphics
    g.drawImage(img, 0, 0, null)

    val w = grayImage.getWidth
    val h = grayImage.getHeight
    val res = (for{
      x <- 1 until w
      y <- 1 until h
    } yield {
      if (grayImage.getRGB(x - 1, y) != grayImage.getRGB(x, y)) {
        if (grayImage.getRGB(x - 1, y) < grayImage.getRGB(x, y)) Some(x - 1, y) else Some(x, y)
      } else if (grayImage.getRGB(x, y - 1) != grayImage.getRGB(x, y)) {
        if (grayImage.getRGB(x, y - 1) < grayImage.getRGB(x, y)) Some(x, y - 1) else Some(x, y)
      } else {
        None
      }
    }).collect{case Some(p) => p}

    res.toSet.toSeq
  }

  def randomFilter(ratio: Double, edges: Seq[(Int, Int)], seed: Long) = {
    val random = new Random(seed)
    val needed = edges.size * ratio
    random.shuffle(edges).take(needed.toInt)
  }
}
