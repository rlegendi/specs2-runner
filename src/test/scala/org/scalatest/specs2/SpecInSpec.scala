package org.scalatest.specs2

import org.specs2.mutable.Specification
import ScalaTestNotifier._
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.runner.JUnitRunner
import org.scalatest.events.LineInFile

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class SpecInSpecTest extends Specification {

  "The outer spec" should {
    "check some other stuff" in {
      success
    }
  }

  class InnerSpec extends Specification {
    "The Inner spec" should {
      "check some cool stuff" in {
        success
      }
    }
  }

  include(new InnerSpec())
}
