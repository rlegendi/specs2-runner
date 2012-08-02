package org.scalatest.bridges

import org.specs2.mutable._
import org.scalatest.WrapWith
import org.scalatest.specs2.Specs2Runner
import org.scalatest.ScalaTestBridge
import org.scalatest.events.IndentedText

@WrapWith(classOf[Specs2Runner])
class ScalaTestBridgeTest extends Specification {
  "The getIndentedText()" should {
    "work properly if decoration is disabled" in {
      "not indent text at level 0" in {
        ScalaTestBridge.getIndentedText("a", 0, false) must have be_== (IndentedText("a", "a", 0))
      }

      "indent text at level 1" in {
        ScalaTestBridge.getIndentedText("a", 1, false) must have be_== (IndentedText("  a", "a", 1))
      }
    }

    "work properly if decoration is enabled" in {
      "not indent decorated text at level 0" in {
        ScalaTestBridge.getIndentedText("a", 0, true) must have be_== (IndentedText("- a", "a", 0))
      }

      // No, this is not a copy & paste typo: it shouldn't be indented
      "not indent decorated text at level 1" in {
        ScalaTestBridge.getIndentedText("a", 1, true) must have be_== (IndentedText("- a", "a", 1))
      }
      
      "indent decorated text at level 2" in {
        ScalaTestBridge.getIndentedText("a", 2, true) must have be_== (IndentedText("  - a", "a", 2))
      }
    }
  }
}
