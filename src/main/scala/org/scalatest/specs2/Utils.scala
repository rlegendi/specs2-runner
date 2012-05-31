package org.scalatest.specs2

import org.specs2.Specification
import org.specs2.specification.SpecificationStructure

object Utils {

  def suiteNameFor(spec: SpecificationStructure): String = {
    if (null == spec) throw new IllegalArgumentException("spec == null")

    val baseName = spec.getClass.getSimpleName
    if (spec.isInstanceOf[Specification])
      return baseName + " specs2 Specification"
    else if (spec.isInstanceOf[org.specs2.mutable.Specification])
      return baseName + " specs2 Unit Specification"
    else
      return baseName + " specs2 Test"
  }

}
