package org.scalatest.specs2

import org.specs2._
import org.scalatest.Filter
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class FirsOfTwoSpecInSameFile extends Specification { def is =

  "This is the first of two specs"                         ^
    "and it should do something cool"                      ! e^
                                                           end
   def e = success
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class SecondOfTwoSpecInSameFile extends Specification { def is =

  "This is the second of two specs"                         ^
    "and it should also do something cool"                 ! e^
                                                           end
   def e = success
}
