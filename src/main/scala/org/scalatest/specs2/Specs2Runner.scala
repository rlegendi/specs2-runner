package org.scalatest.specs2

import org.specs2.reporter.NotifierReporter
import org.specs2.reporter.DefaultSelection
import org.scalatest.Distributor
import org.specs2.specification.SpecificationStructure
import org.scalatest.Stopper
import org.scalatest.Reporter
import org.scalatest.Suite
import org.specs2.main.Arguments
import org.scalatest.Style
import org.scalatest.Filter
import org.scalatest.Tracker
import org.scalatest.events.InfoProvided
import org.scalatest.specs2.notifiers.ScalaTestNotifier
import org.specs2.Specs2Bridge._
import org.scalatest.Resources
import org.scalatest.events.NameInfo
import org.specs2.specification.Fragments

/**
 * The central concept in ScalaTest is the suite, a collection of zero to many tests.
 *
 * <p>
 * This is a Suite that can handle both specs2 acceptance specifications and (mutable
 * unit specifications.
 * </p>
 *
 * @author rlegendi
 * @constructor create a new runner for the specified specification class;
 * 					<i>cannot be null</i>
 */
// Note: Avoid methods whose name starts with "test", those are handled as tests.
@Style("org.scalatest.specs.Specs2Finder")
class Spec2Runner(specs2Class: Class[_ <: SpecificationStructure]) extends Suite {
  require(specs2Class != null)

  /** The specification instance to run. */
  protected lazy val spec2 = tryToCreateSpecification(specs2Class)

  /**
   * Name of the suite.
   *
   * <p>
   * This is a bit tricky: the start of the suite is automatically reported by ScalaTest,
   * so we need a bit of playing with the scope stack in {@link ScalaTestNotifier} (we do
   * not report the start of the outermost suite and do not fire its close event, because
   * it terminates the simulation. All this is required because of nested specifications
   * in the same compilation unit (e.g., multiple Specifications is the same
   * <code>*.scala</code> file, embedded Specifications, etc.).
   * </p>
   */
  override def suiteName = Utils.suiteNameFor(spec2)

  /** Unique Id for the suite for ScalaTest. */
  override def suiteId = Utils.suiteIdFor(spec2)

  /** Holds all the options that are relevant for the execution (and reporting). */
  protected implicit lazy val args: Arguments = spec2.arguments

  /**
   * Returns the expected number of tests.
   *
   * <p>
   * Eric suggested that the best thing here is to reuse the code already in the Selection
   * trait of specs2: the idea is to use ScalaTest options and translate them to specs2
   * arguments, as if they were passed from the command line.
   * </p>
   *
   * @param filter filter to use; <i>cannot be null</i>
   * @return the number of sepcs2 examples found for the specification; <i>non-negative</i>
   */
  // TODO ScalaTest's dynatags here are not used because we're not sure it's absolutely necessary to provide filtering capabilities based on tags
  override def expectedTestCount(filter: Filter): Int = {
    require(filter != null)

    // The <| method is used to override arguments. Note that I'm using the spec arguments to override the command line ones
    // so that a local definition of arguments in a specification can override generic arguments on the command line

    val arguments = Arguments(filter.tagsToInclude.map(tags => "include " + tags.mkString(",")) + " " +
      filter.tagsToExclude.mkString("exclude ", ",", "")) <| args

    val selection = new DefaultSelection {}

    // There are methods in the Fragments object to filter specific fragments, like isAnExample
    selection.select(arguments)(spec2).fragments.collect(Fragments.isAnExample).size
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

    // Just to make sure exceptions are properly handled during the test
    val report = wrapReporterIfNecessary(reporter)

    runSpec2(tracker, reporter, filter)

    if (stopper()) { // If stopped during execution
      report(InfoProvided(tracker.nextOrdinal(), Resources("executeStopping"), Some(NameInfo(suiteName, Some(this.getClass.getName), testName))))
    }
  }

  // TODO Use filter?
  private[specs2] def runSpec2(tracker: Tracker, reporter: Reporter, filter: Filter): Unit = {
    require(tracker != null)
    require(reporter != null)
    require(filter != null)

    // TODO Here we get where the whole thing starts, needs checking (report execution started?)
    new NotifierReporter {
      val notifier = new ScalaTestNotifier(spec2, args, tracker, reporter)
    }.report(spec2)(args)
  }
}
