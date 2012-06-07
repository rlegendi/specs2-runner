package org.scalatest.specs2

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MyClassSpec extends SpecificationWithJUnit {

  "MyClass" should {
    "do something" in {
      val sut = "MyClass()"
      //sut.doIt must_== "OK"
      success
    }

    args(skipAll = false)
    "do something with db" in {
      success
      // Check only if db is running, SKIP id not
    }
  }

}
