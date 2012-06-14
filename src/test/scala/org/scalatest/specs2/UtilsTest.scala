package org.scalatest.specs2

import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure

// These are the subjects to test

class DummySpecification extends org.specs2.Specification {
  def is = success
}

class DummyMutableSpecification extends Specification

class DummyMutableSpecificationWithTitle extends Specification {
  "Mutable Title".title
}

class DummyNonExistentSpecification extends SpecificationStructure {
  def is = new Fragments
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Spec2Runner])
class UtilsTest extends Specification {

  "In the Utils class".title

  "The hasTitle() function" should {
    "throw an exception for null" in {
      Utils.hasTitle(null) must throwA[IllegalArgumentException]
    }

    "return false for unit spec without title" in {
      Utils.hasTitle(new DummyMutableSpecification) must beFalse
    }

    "return true for unit spec with title" in {
      Utils.hasTitle(new DummyMutableSpecificationWithTitle) must beTrue
    }

  }

  "The suiteNameFor() function" should {
    "throw an exception for null" in {
      Utils.suiteNameFor(null) must throwA[IllegalArgumentException]
    }

    "append 'Acceptance Specification'" in {
      Utils.suiteNameFor(new DummySpecification) must be_==("DummySpecification Acceptance Specification")
    }

    "append 'Unit Specification'" in {
      Utils.suiteNameFor(new DummyMutableSpecification) must be_==("DummyMutableSpecification Unit Specification")
    }

    "append 'Specification'" in {
      Utils.suiteNameFor(new DummyNonExistentSpecification) must be_==("DummyNonExistentSpecification Specification")
    }
  }

  "The suiteIdFor() function" should {
    "throw an exception for null" in {
      Utils.suiteIdFor(null) must throwA[IllegalArgumentException]
    }

    "return proper result for an acceptance specification'" in {
      Utils.suiteIdFor(new DummySpecification) must be_==("org.scalatest.specs2.DummySpecification")
    }
  }
}
