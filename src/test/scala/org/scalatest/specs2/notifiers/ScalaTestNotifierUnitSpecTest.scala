package org.scalatest.specs2.notifiers

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.runner.JUnitRunner
import org.scalatest.specs2.Spec2Runner
import ScalaTestNotifier._
import org.scalatest.events.LineInFile

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class ScalaTestNotifierUnitSpecTest extends Specification {
  "The locator function" should {
    "require non-null parameter" in {
      loc(null) must throwA[IllegalArgumentException]
    }

    "return a proper location for a simple case" in {
      val spec2Loc = "org.scalatest.specs2.Spec2Runner (Spec2Runner.scala:164)"
      loc(spec2Loc) must be_==(Some(LineInFile(164, "Spec2Runner.scala")))
    }

    "return a proper location for a file with several spaces" in {
      val spec2Loc = "org.scalatest.specs2.Spec2Runner ( Spec 2 Runner .scala :164)"
      loc(spec2Loc) must be_==(Some(LineInFile(164, " Spec 2 Runner .scala ")))
    }

    "return None for any malformed input" in {
      loc("Hit me baby one more time") must be_==(None)
    }
  }
}
