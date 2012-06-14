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
import org.specs2.specification.SpecificationStructure
import org.specs2.reflect.Classes

/**
 * The central concept in ScalaTest is the suite, a collection of zero to many tests.
 *
 * This is a Suite that can handle both mutable and immutable specs2 specifications
 *
 * @author rlegendi
 */
// Note: Avoid methods whose name starts with "test", those are handled as tests.
@Style("org.scalatest.specs.Spec2Finder")
class Spec2Runner(specs2Class: Class[_ <: SpecificationStructure]) extends Suite {
  require(specs2Class != null)

  protected lazy val spec2 = tryToCreateSpecification(specs2Class)

  /**
   * 
   */
  override def suiteName = Utils.suiteNameFor(spec2)

  /** Unique id the full class name of the specification. */
  override def suiteId = Utils.suiteIdFor(spec2)

  //  println(suiteName)
  //  println(suiteId)

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

    val arguments = Arguments(filter.tagsToInclude.map(tags => "include " + tags.mkString(",")) + " " +
      filter.tagsToExclude.mkString("exclude ", ",", "")) <| args

    // There are methods in the Fragments object to filter specific fragments, like isAnExample
    selection.select(arguments)(spec2).fragments.collect(isAnExample).size
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

    val stopRequested = stopper // TODO Can't this be done below at [1]?
    val report = wrapReporterIfNecessary(reporter) // TODO Can't this be done below where report() is used?

    //report(RunStarting()) //?

    runSpec2(tracker, reporter, filter)

    if (stopRequested()) { // [1]
      val rawString = Resources("executeStopping") // TODO Do I need it here? Or this is just a descriptive variable?
      report(InfoProvided(tracker.nextOrdinal(), rawString, Some(NameInfo(suiteName, Some(this.getClass.getName), testName))))
    }
  }

  // TODO Use filter?
  private[specs2] def runSpec2(tracker: Tracker, reporter: Reporter, filter: Filter): Unit = {
    new NotifierReporter {
      val notifier = new ScalaTestNotifier(spec2, args, tracker, reporter)
      //val notifier = new EmptyNotifier
    }.report(spec2)(args)
  }
}

/** Used for testing purposes only. */
private[specs2] object Spec2Runner {
  def apply(specs2Class: Class[_ <: SpecificationStructure]) = new Spec2Runner(specs2Class)
}
