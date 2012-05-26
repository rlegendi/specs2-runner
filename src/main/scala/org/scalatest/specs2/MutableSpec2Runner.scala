package org.scalatest.specs2

import org.scalatest.Distributor
import org.scalatest.Filter
import org.scalatest.Reporter
import org.scalatest.Stopper
import org.scalatest.Style
import org.scalatest.Suite
import org.scalatest.Tracker
import org.specs2.Specs2Bridge.getContentFor
import org.specs2.Specs2Bridge.tryToCreateObject
import org.specs2.main.Arguments
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitDescriptionsFragments
import org.specs2.specification.Action
import org.specs2.specification.Example
import org.specs2.specification.SpecificationStructure
import org.specs2.specification.Step
import org.specs2.text.MarkupString

/**
 * The central concept in ScalaTest is the suite, a collection of zero to many tests.
 *
 * @author rlegendi
 */
// Note: Avoid methods whose name starts with "test", those are handled as tests.
// TODO Handle mutable specifications
// TODO Specs2 arguments?
//class MutableSpec2Runner(specs2Class: Class[_ <: Specification]) extends Suite with DefaultSelection with DefaultSequence with Exporters {
@Style("org.scalatest.specs.Spec2Finder") //@Style("org.specs2.Spec2Finder")
class MutableSpec2Runner(specs2Class: Class[_ <: Specification]) extends Suite {

  //val spec2 = specs2Class.newInstance()

  protected lazy val spec2 = tryToCreateObject[SpecificationStructure](specs2Class.getName).get

  override def suiteName = specs2Class.getSimpleName

  override def suiteId = specs2Class.getName

  private val descriptions = new JUnitDescriptionsFragments(specs2Class.getName)

  //implicit lazy val args: Arguments = spec2.content.arguments
  implicit lazy val args: Arguments = getContentFor(spec2).arguments

  //private lazy val DescriptionAndExamples(desc, executions) = descriptions.foldAll((select(args)(specification) |> sequence).fragments)

  override def expectedTestCount(filter: Filter): Int = {
    //getContentFor(spec2).fragments.count(f => f.isInstanceOf[Example] || f.isInstanceOf[Step] || f.isInstanceOf[Action])
    getContentFor(spec2).fragments.count { f =>
      {
        if (f.isInstanceOf[Example]) {
          // TODO Ask Eric: Why do I need toString here? I get an error otherwise (overloaded method value apply with alternatives: ...)
          // I cannot create an implicit because MarkupString is also package-private

          // Usage copied from here: http://www.scalatest.org/scaladoc/1.3/org/scalatest/Filter.html
          // TODO Ask Chee Seng why he didn't need to check the ignore flag
          val (filterExample, ignoreTest) = filter(f.asInstanceOf[Example].desc.toString, tags, suiteId)

          !filterExample && !ignoreTest
        } else {
          false
        }
      }
    }
  }

  /*
  private def getSpecTestCount(theSpec: Specification, filter: Filter): Int = {

  }

  private def getSusTestCount(sus: Sus, filter: Filter, scopeStack: Stack[String]): Int = {
    val sysDesc = sus.description + " " + sus.verb
    if (scopeStack.isEmpty)
      scopeStack.push(sysDesc)
    else
      scopeStack.push(scopeStack.head + " " + sysDesc)

    val count = sus.examples.foldLeft(0)(_ + getExampleTestCount(_, filter, scopeStack))

    scopeStack.pop()

    count
  }

  private def getExampleTestCount(example: Example, filter: Filter, scopeStack: Stack[String]): Int = {
    if (example.hasSubExamples) {
      if (scopeStack.isEmpty)
        scopeStack.push(example.description)
      else
        scopeStack.push(scopeStack.head + " " + example.description)

      val count = example.examples.map(getExampleTestCount(_, filter, scopeStack)).foldLeft(0)(_ + _) + 1

      scopeStack.pop()
      count
    } else {
      val testName = getTestName(scopeStack, example.description)
      val (filterExample, ignoreTest) = filter(testName, tags, suiteId)
      if (!filterExample)
        1
      else
        0
    }
    /*if(example.hasSubExamples)
      example.examples.map(getExampleTestCount(_)).foldLeft(0)(_ + _)
    else
      example.ownExpectationsNb*/
  }

  private def getTestName(scopeStack: Stack[String], testText: String) = {
    if (scopeStack.isEmpty)
      testText
    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
      scopeStack.head + " " + testText
  }

  override def expectedTestCount(filter: Filter): Int = getSpecTestCount(spec, filter) //spec.firstLevelExamplesNb
  */

  //  override def expectedTestCount(filter: Filter): Int = {
  //    //spec2.content.
  //    println( spec2.content )
  //    0
  //  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
    configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    println("I got you baby")

    if (null == testName) throw new IllegalArgumentException("testName == null")
    if (null == reporter) throw new IllegalArgumentException("reporter ==null")
    if (null == stopper) throw new IllegalArgumentException("stopper == null")
    if (null == filter) throw new IllegalArgumentException("filter == null")
    if (null == configMap) throw new IllegalArgumentException("configMap == null")
    if (null == distributor) throw new IllegalArgumentException("distributor == null")
    if (null == tracker) throw new IllegalArgumentException("tracker == null")

    val stopRequested = stopper // TODO Can't this be done below at [1]?
    //    val report = wrapReporterIfNecessary(reporter) // TODO Can't this be done below where report() is used?
    //
    //    runSpec(Some(spec2), tracker, reporter, filter)
    //
    //    if (stopRequested()) { // [1]
    //      val rawString = Resources("executeStopping")
    //      report(InfoProvided(tracker.nextOrdinal(), rawString, Some(NameInfo(suiteName, Some(this.getClass.getName), testName))))
    //    }
  }

  private def runSpec(specification: Option[SpecificationStructure], tracker: Tracker, reporter: Reporter, filter: Filter): Option[Specification] = {
    //    def testInterfaceRunner(s: Specification) = new ScalaTestNotifierRunner(s, new ScalaTestNotifier(spec, tracker, reporter), filter, tags, suiteId)
    //    specification.map(testInterfaceRunner(_).reportSpecs)
    //    specification match {
    //      case Some(s: org.specs.runner.File) => s.reportSpecs
    //      case _ => ()
    //    }
    //    specification
    println("I got you baby!")
    None
  }

}

object MutableSpec2Runner {
  def apply(specs2Class: Class[_ <: Specification]) = new MutableSpec2Runner(specs2Class)
}
