package org.scalatest.specs2

import org.junit.runner.RunWith
import org.scalatest.Filter
import org.scalatest.Style
import org.scalatest.WrapWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner

// This is the unit specification under test
class HelloWorldUnitSpec extends mutable.Specification {
  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size (11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class HelloWorldUnitSpecTest extends mutable.Specification {

  val runner = Spec2Runner(classOf[HelloWorldUnitSpec])

  "When executing 'HelloWorldUnitSpec' the runner" should {
    "find 3 examples" in {
      val noFilter = new Filter(None, Set.empty[String])
      runner.expectedTestCount(noFilter) must be_==(3)
    }
  }
}
