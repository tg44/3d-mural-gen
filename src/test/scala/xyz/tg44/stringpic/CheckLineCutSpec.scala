package xyz.tg44.stringpic

import org.scalatest.{Matchers, WordSpecLike}

class CheckLineCutSpec extends WordSpecLike with Matchers {


  "CheckLintCut" should {
    "fail s1" in {
      val p1 = (1, 1);
      val q1 = (10, 1);
      val p2 = (1, 2);
      val q2 = (10, 2);
      StringPicGenerator.checkLineCut((p1, q1), (p2, q2)) shouldBe false
    }
    "ok" in {
      val p1 = (10, 1);
      val q1 = (0, 10);
      val p2 = (0, 0);
      val q2 = (10, 10);
      StringPicGenerator.checkLineCut((p1, q1), (p2, q2)) shouldBe true
    }

    "fail s2" in {
      val p1 = (-5, -5);
      val q1 = (0, 0);
      val p2 = (1, 1);
      val q2 = (10, 10);
      StringPicGenerator.checkLineCut((p1, q1), (p2, q2)) shouldBe false
    }
  }
}
