package xyz.tg44.stringpic

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import org.scalatest.{Matchers, WordSpecLike}

class CheckConvexitySpec extends WordSpecLike with Matchers{

  "CheckConvexity" should {
    val imageFile = new File("africa.png")
    val img = ImageIO.read(imageFile)
    val grayImage = new BufferedImage(img.getWidth(), img.getHeight(),
      BufferedImage.TYPE_BYTE_BINARY);
    val g = grayImage.createGraphics
    g.drawImage(img, 0, 0, null)

    "reachable is ok" in {
      StringPicGenerator.checkConvexity(grayImage, (66,57), (130,85)) shouldBe true
    }

    "non reachable is nok" in {
      StringPicGenerator.checkConvexity(grayImage, (45,70), (100,160)) shouldBe false
    }

    "non reachable is nok v2" in {
      StringPicGenerator.checkConvexity(grayImage, (188,82), (188,170)) shouldBe false
    }
  }

}
