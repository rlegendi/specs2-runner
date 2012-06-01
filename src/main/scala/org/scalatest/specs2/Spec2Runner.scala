package org.scalatest.specs2

import org.scalatest.Distributor
import org.scalatest.Filter
import org.scalatest.Reporter
import org.scalatest.Stopper
import org.scalatest.Style
import org.scalatest.Suite
import org.scalatest.Tracker
import org.specs2.Specs2Bridge._
import org.specs2.main.Arguments
import org.specs2.mutable.Specification
import org.specs2.specification.Action
import org.specs2.specification.Example
import org.specs2.specification.SpecificationStructure
import org.specs2.specification.Step
import org.specs2.text.MarkupString
import org.scalatest.Resources
import org.specs2.specification.SpecStart
import org.specs2.specification.Text
import org.specs2.specification.SpecEnd
import org.specs2.specification.SpecStart
import org.specs2.specification.FragmentExecution
import org.specs2.specification.ExecutedFragment
import org.specs2.specification.ExecutingSpecification
import org.specs2.TSpec2IntegrationExporters
import org.specs2.main.Arguments
import org.specs2.specification.ExecutedResult
import scala.reflect.NameTransformer
import org.specs2.specification.ExecutedResult
import org.specs2.specification.ExecutedFragment
import org.specs2.specification.Fragments._
import org.specs2.runner.{ NotifierRunner, JUnitDescriptionsFragments }
import org.specs2.execute.Details
import org.specs2.reporter.{ NotifierReporter, Notifier, DefaultSelection }
import org.scalatest.events._

/**
 * The central concept in ScalaTest is the suite, a collection of zero to many tests.
 *
 * This is a Suite that can handle both mutable and immutable specs2 specifications
 *
 * @author rlegendi
 */
// Note: Avoid methods whose name starts with "test", those are handled as tests.
// TODO Specs2 arguments?
// TODO with DefaultSelection with DefaultSequence with Exporters?
@Style("org.scalatest.specs.Spec2Finder")
class Spec2Runner(specs2Class: Class[_ <: SpecificationStructure]) extends Suite with TSpec2IntegrationExporters {

  protected lazy val spec2 = tryToCreateObject[SpecificationStructure](specs2Class.getName).get

  /**
   * The specification title is more appropriate than the class name for a <code>suiteName</code>
   * because the user can specify a more readable name for its specification.
   */
  override def suiteName = spec2.identification.title // Utils.suiteNameFor(spec2)

  /** ERIC: fullName is the full class name of the specification */
  override def suiteId = Utils.suiteIdFor(spec2)

  // TODO Why do I need the {}s here?
  // ERIC: because I left the FragmentExecution object hidden, there's actually no compelling reason to do that
  protected val executor = new FragmentExecution {}

  // TODO Content is package-private, this is a workaround, consult with Eric
  // ERIC: that's intentional. It is to avoid the namespace of the Specification inheritor to be polluted with something he never uses
  // one way to make things nicer is to add "implicit" to your getContent definition. This way, any spec can be seen as the list
  // of Fragments it is holding
  protected implicit lazy val args: Arguments = spec2.arguments

  protected val selection = new DefaultSelection {}

  override def expectedTestCount(filter: Filter): Int = {
    // ERIC: I think that the best thing here is to reuse the code already in the Selection trait of specs2
    // the idea is to use ScalaTest options and translate them to specs2 arguments, as if they were passed from the command line
    // I'm not using ScalaTest's dynatags here because I'm not sure it's absolutely necessary to provide filtering capabilities based on tags
    //
    // the <| method is used to override arguments. Note that I'm using the spec arguments to override the command line ones
    // so that a local definition of arguments in a specification can override generic arguments on the command line

    /**
     * val arguments = Arguments(filter.tagsToInclude.map(tags => "include "+tags.mkString(","))+" "+
     * filter.tagsToExclude.mkString("exclude ", ",", "")) <| args
     */

    println(">> ARGS: " + (filter.tagsToInclude.map(tags => "include " + tags.mkString(",")) + " " +
      filter.tagsToExclude.mkString("exclude ", ",", "")))

    val arguments = parseArguments(List(filter.tagsToInclude.map(tags => "include " + tags.mkString(",")) + " " +
      filter.tagsToExclude.mkString("exclude ", ",", ""))) <| args

    // There are methods in the Fragments object to filter specific fragments, like isAnExample
    //selection.select(arguments)(spec2.fragments).fragments.collect(isAnExample).size
    selection.select(arguments)(spec2).fragments.collect(isAnExample).size

    //    spec2.fragments.count { f =>
    //      {
    //        if (f.isInstanceOf[Example]) {
    //          // TODO Ask Eric: Why do I need toString here? I get an error otherwise (overloaded method value apply with alternatives: ...)
    //          // ERIC: you get an error because desc is not a String. The error message says that none of the 2 apply methods works with the MarkupString type
    //          // I cannot create an implicit because MarkupString is also package-private
    //          // Should I use some inner class like Descriptor or something?
    //
    //          // Usage copied from here: http://www.scalatest.org/scaladoc/1.3/org/scalatest/Filter.html
    //          // TODO Ask Chee Seng why he didn't need to check the ignore flag
    //          val (filterExample, ignoreTest) = filter(f.asInstanceOf[Example].desc.toString, tags, suiteId)
    //
    //          !filterExample && !ignoreTest
    //        } else {
    //          false
    //        }
    //      }
    //    }
  }

  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
    configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

    require(testName != null)
    require(reporter != null)
    require(stopper != null)
    require(filter != null)
    require(configMap != null)
    require(distributor != null)
    require(tracker != null)

    // ERIC: I don't really get that part for now
    val stopRequested = stopper // TODO Can't this be done below at [1]?
    val report = wrapReporterIfNecessary(reporter) // TODO Can't this be done below where report() is used?

    //report(RunStarting()) //?

    runSpec2(tracker, reporter, filter)

    if (stopRequested()) { // [1]
      val rawString = Resources("executeStopping") // TODO Do I need it here? Or this is just a descriptive variable?
      report(InfoProvided(tracker.nextOrdinal(), rawString, Some(NameInfo(suiteName, Some(this.getClass.getName), testName))))
    }
  }

  protected def runSpec2(tracker: Tracker, reporter: Reporter, filter: Filter): Unit = {
    //    def testInterfaceRunner(s: Specification) = new ScalaTestNotifierRunner(s, new ScalaTestNotifier(spec, tracker, reporter), filter, tags, suiteId)
    //    specification.map(testInterfaceRunner(_).reportSpecs)
    //    specification match {
    //      case Some(s: org.specs.runner.File) => s.reportSpecs
    //      case _ => ()
    //    }
    //    specification

    //executeSpecifications |> export |> notifyScalaTest(notifier)
    // TODO In the original code (specs2.JUnitRunner) this worked (it was a Stream), but here it fails with a compilation error:
    // ERIC: this fails because |> is a convenience operator provided by Scalaz. You can however use regular functions:
    // notifyScalaTest(export(executeSpecifications))

    //		value |> is not a member of Seq[...]
    //executeSpecifications |> export |> notifyScalaTest()

    // TODO Should I pass specification here? isn't that
    //    notifyScalaTest(spec2, tracker, reporter)( export( executeSpecifications ) ) // TODO This is kinda... ugly?

    // ERIC: using a NotifierReporter might be the best approach for now
    new NotifierReporter {
      val notifier = new ScalaTestNotifier(tracker, reporter)
    }.report(spec2)(args)
  }
  //
  //  private def executeSpecifications : Seq[ExecutedFragment] =
  //    spec2.fragments collect {
  //      case f @ SpecStart(_, _, _) => executor.executeFragment(args)(f)
  //      case f @ Example(_, _) => executor.executeFragment(args)(f)
  //      case f @ Text(_) => executor.executeFragment(args)(f)
  //      case f @ Step(_) => executor.executeFragment(args)(f)
  //      case f @ Action(_) => executor.executeFragment(args)(f)
  //      case f @ SpecEnd(_) => executor.executeFragment(args)(f)
  //      //case _                     => None // TODO Is this a correct approach?
  //    }
  //
  //  private def export = (executed: Seq[ExecutedFragment]) => {
  //    //val commandLineArgs = properties.getProperty("commandline").getOrElse("").split("\\s")
  //    val commandLineArgs = "".split("\\s")
  //    //def exportTo = (name: String) => properties.isDefined(name) || commandLineArgs.contains(name)
  //    def exportTo = (name: String) => false;
  //
  //    val executedSpecification = createExecuteSpecification(spec2.specName, executed)
  //    // TODO Ask Eric what's happening here :-)
  //    exportToOthers(parseArguments(commandLineArgs) <| args, exportTo)(executedSpecification)
  //    executed
  //  }
  //
}

/*
  private def executeSpecifications : Seq[ExecutedFragment] =
    //getContentFor(spec2).fragments foreach ( _ match {
    getContentFor(spec2).fragments collect {
      case f @ SpecStart(_, _, _) => executor.executeFragment(args)(f)
      case f @ Example(_, _) => executor.executeFragment(args)(f)
      case f @ Text(_) => executor.executeFragment(args)(f)
      case f @ Step(_) => executor.executeFragment(args)(f)
      case f @ Action(_) => executor.executeFragment(args)(f)
      case f @ SpecEnd(_) => executor.executeFragment(args)(f)
      //case _                     => None // TODO Is this a correct approach?
    }
  private def executeSpecifications: Seq[ExecutedFragment] =
    //getContentFor(spec2).fragments foreach ( _ match {
    getContentFor(spec2).fragments collect {
      case f @ SpecStart(_, _, _) => executor.executeFragment(args)(f)
      case f @ Example(_, _) => executor.executeFragment(args)(f)
      case f @ Text(_) => executor.executeFragment(args)(f)
      case f @ Step(_) => executor.executeFragment(args)(f)
      case f @ Action(_) => executor.executeFragment(args)(f)
      case f @ SpecEnd(_) => executor.executeFragment(args)(f)
      //case _                     => None // TODO Is this a correct approach?
    }
*/


/*
    val executedSpecification = createExecuteSpecification(getContentFor(spec2).specName, executed)
    // TODO Ask Eric what's happening here :-)
    exportToOthers(parseArguments(commandLineArgs) <| args, exportTo)(executedSpecification)
    executed
  }
*/

//}

/** Used for testing purposes only. */
private[specs2] object Spec2Runner {
  def apply(specs2Class: Class[_ <: SpecificationStructure]) = new Spec2Runner(specs2Class)
}
