package org.scalatest.specs2

import org.junit.runner.RunWith
import org.scalatest.Style
import org.scalatest.WrapWith
import org.scalatest.specs2.output.OutputUtils.runSpecAndReturnReversedScopeStack
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

// Test subject
class SimpleSpec extends Specification {
  "The ScalaTest API" should {
    "have the Runner.doRunRunRunDaDoRunRun() because it's funny" in {
      success
    }
  }
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class ReportingOutputUnitSpecTest extends Specification {

  "The ScalaTest Specs2 runner" should {
    val reversedOutputStack = runSpecAndReturnReversedScopeStack(classOf[SimpleSpec])

    "report 2 levels of output" in {
      reversedOutputStack.size must be_==(2)
    }

    "where the first element" in {
      val e0 = reversedOutputStack(0)

      "must be at the 0th indentation level" in {
        e0.lvl must be_==(0)
      }

      "must contain the proper 'should' declaration" in {
        e0.msg must be_==("The ScalaTest API should")
      }
    }

    "where the second element" in {
      val e1 = reversedOutputStack(1)

      "must be at the 1st indentation level" in {
        e1.lvl must be_==(1)
      }

      "must contain the proper 'should' declaration" in {
        e1.msg must be_==("have the Runner.doRunRunRunDaDoRunRun() because it's funny")
      }
    }
  }
}
