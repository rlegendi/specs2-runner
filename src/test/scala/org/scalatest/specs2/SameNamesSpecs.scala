package org.scalatest.specs2

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class SameNamesSpecs extends Specification {
  "One" should {
    "a" in {
      success
    }
  }

  "Other" should {
    "a" in {
      success
    }
  }
}
