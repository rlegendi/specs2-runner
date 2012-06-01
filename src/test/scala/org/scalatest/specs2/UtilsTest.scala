package org.scalatest.specs2

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.SpecificationStructure
import org.specs2.specification.Fragments

// This is the unit specification under test
class DummySpecification extends org.specs2.Specification {
  def is = success
}

class DummyMutableSpecification extends Specification

class DummyNonExistentSpecification extends SpecificationStructure {
  def is = new Fragments
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class UtilsTest extends Specification {

  // TODO Check difference between .br and .txt
  // TODO What is the proper way in specs2 to create sub-contexts?
  "In the Utils class".title

  "The suiteNameFor() function" should {
    "throw an exception for null" in {
      Utils.suiteNameFor(null) must throwA[IllegalArgumentException]
    }

    "append specs2 Specification" in {
      Utils.suiteNameFor(new DummySpecification) must be_==("DummySpecification specs2 Specification")
    }

    "append specs2 Unit Specification'" in {
      Utils.suiteNameFor(new DummyMutableSpecification) must be_==("DummyMutableSpecification specs2 Unit Specification")
    }

    "append 'specs2 Test'" in {
      Utils.suiteNameFor(new DummyNonExistentSpecification) must be_==("DummyNonExistentSpecification specs2 Test")
    }

    "this is the first major example" >> { ok }
    "this is minor and should be indented" >> { ok }
    "this is the second major example" >> { ok }
  }
  
  "a" should {
    "f" in {
      "a" in {
        success
      }
    }

    "g" in {
      "b" in {
        success
      }
    }
  }
}
