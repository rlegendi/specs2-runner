package org.scalatest.specs2

import org.specs2._
import org.scalatest.Filter
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.runner.JUnitRunner

class HelloWorldSpec extends Specification { def is =

  "This is a specification to check the 'Hello world' string"                 ^
                                                                              p^
  "The 'Hello world' string should"                                           ^
    "contain 11 characters"                                                   ! e1^
    "start with 'Hello'"                                                      ! e2^
    "end with 'world'"                                                        ! e3^
                                                                              end

  def e1 = "Hello world" must have size(11)
  def e2 = "Hello world" must startWith("Hello")
  def e3 = "Hello world" must endWith("world")
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class HelloWorldSpecTest extends Specification { def is =

  "This is a specification for HelloWorldSpec as a standard specification"    ^
                                                                              p^
  "When executing 'HelloWorldSpec' the runner should"                         ^
    "find 3 examples"                                                         ! threeExamplesFound^
                                                                              end
   val noFilter = new Filter(None, Set.empty[String])
   val runner = Spec2Runner(classOf[HelloWorldSpec])

   def threeExamplesFound = runner.expectedTestCount(noFilter) must be_==(3)
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class HelloWorldSpecTest2 extends Specification { def is =

  "This is another Specification"    ^
                                                                   p^
  "When executing The other specification"                         ^
    "something cool should happen"                                 ! e^
                                                                   end
   def e = success
}
