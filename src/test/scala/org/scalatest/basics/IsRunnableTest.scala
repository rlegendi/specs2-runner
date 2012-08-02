package org.scalatest.basics

import org.junit.runner.RunWith
import org.scalatest.WrapWith
import org.scalatest.specs2.output.ReportingOutputUnitSpecTest
import org.scalatest.specs2.Specs2Runner
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

// Test subject
class SomeOtherSimpleSpec extends Specification {
  "The ScalaTest API" should {
    "have the Runner.doRunRunRunDaDoRunRun() because it's funny" in {
      success
    }
  }
}

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Specs2Runner])
class IsRunnableTest extends Specification {
  def isRunnable(clazz: java.lang.Class[_]): Boolean = {
    val wrapWithAnnotation = clazz.getAnnotation(classOf[WrapWith])
    if (wrapWithAnnotation != null) {
      val wrapperSuiteClazz = wrapWithAnnotation.value
      val constructorList = wrapperSuiteClazz.getDeclaredConstructors()
      constructorList.exists { c =>
        val types = c.getParameterTypes
        types.length == 1 && types(0) == classOf[java.lang.Class[_]]
      }
    } else {
      return false
    }
  }

  "If running Specs2 through ScalaTest package discovery" should {
    "Find ROUST" in {
      isRunnable(classOf[ReportingOutputUnitSpecTest]) must beTrue
    }
  }
}