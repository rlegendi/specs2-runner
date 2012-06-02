package org.specs2

import org.specs2.reflect.Classes
import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure
import org.specs2.main.Arguments
import org.specs2.specification.ExecutingSpecification
import org.specs2.specification.ExecutingFragment
import org.specs2.specification.SpecName
import org.specs2.execute.Pending
import org.specs2.specification.ExecutedResult
import org.specs2.execute.DecoratedResult
import org.specs2.execute.Skipped
import org.scalatest.Reporter
import org.specs2.execute.Failure
import org.specs2.execute.Success
import org.scalatest.Tracker
import org.scalatest.events.TestStarting
//import reporter.Reporter
import scala.reflect.NameTransformer
import org.scalatest.events.MotionToSuppress
import org.specs2.specification.ExecutedSpecStart
import org.specs2.specification.ExecutedSpecEnd
import org.scalatest.events.TestSucceeded
import org.scalatest.Suite
import org.scalatest.ScalaTestBridge
import org.scalatest.events.TestFailed
import org.specs2.execute.Error
import org.scalatest.events.TestPending
import org.scalatest.specs2.Utils
import org.specs2.specification.ExecutedFragment

object Specs2Bridge {
  def tryToCreateObject[T <: AnyRef](className: String, printMessage: Boolean = true, printStackTrace: Boolean = true,
    loader: ClassLoader = Thread.currentThread.getContextClassLoader)(implicit m: Manifest[T]): Option[T] = {

    Classes.tryToCreateObject(className, printMessage, printStackTrace, loader)(m)
  }

  implicit def getContentFor(spec: SpecificationStructure): Fragments = spec.content;

  def createExecuteSpecification(name: SpecName, fs: Seq[ExecutedFragment], args: Arguments = Arguments()): ExecutingSpecification =
    ExecutingSpecification.create(name, fs, args)

  // TODO This is exact copy&paste code from ScalaTestNotifier - should it be publicly visible?
  private def getDecodedName(name: String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if (decoded == name) None else Some(decoded)
  }

  def notifyScalaTest(spec2: SpecificationStructure, tracker: Tracker, reporter: Reporter) = (executed: Seq[ExecutedFragment]) => {

    // TODO Probably I will need some nice descriptions like JUnitDescriptionsFragments.mapper() has

    // TODO Chee Seng: Why are we calling nextOrdinal() for a new suite name each time?

    // TODO Utility methods for suiteId( spec ) = spec.getClass.getSimpleName, etc.

    executed foreach {
      case (res @ ExecutedResult(_, result, timer, _, _)) => {
        //notifier.fireTestStarted(desc)

        val testName = res.text(spec2.content.arguments).toString
        val exampleName = testName + " (exampleName)"

        //val duration = res.stats.time.toInt //System.currentTimeMillis() - exampleStart
        // TODO Is this the correct time to use here?
        val duration = timer.totalMillis
        
        //val formatter = ScalaTestBridge.getIndentedText(exampleName, indentLevel + 1, true)
        val formatter = ScalaTestBridge.getIndentedText(testName, 2, true)

        res.message // TODO I should use this
        
        val name = Utils.suiteNameFor(spec2)
        val id = Utils.suiteIdFor(spec2)

        reporter(TestStarting(tracker.nextOrdinal(), name, id, Some(id),
          getDecodedName(name), testName, exampleName,
          getDecodedName(testName), Some(MotionToSuppress), None, Some(id)))

        /* TODO Required ScalaTest notifications:
           * TestStarting ---
           * TestSucceeded ---
           * TestFailed ---
           * TestIgnored
           * TestPending ---
           * TestCanceled
           * SuiteStarting
           * SuiteCompleted
           * SuiteAborted
           * RunStarting
           * RunCompleted
           * RunStopped
           * RunAborted
           * InfoProvided
           * MarkupProvided
           * ScopeOpened ---
           * ScopeClosed ---
           */

        // TODO Ask Eric: When do the DecoratedResults are used?
        // ERIC: this is used to display the html table for a DataTable when reporting to html files

        // TODO Indenting?

        result match {
          case f @ Failure(m, e, st, d) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            //reporter(TestFailed(tracker.nextOrdinal(), e.getMessage, spec.getClass.getSimpleName, spec.getClass.getName, Some(spec.getClass.getName), getDecodedName(spec.getClass.getSimpleName), testName, exampleName, getDecodedName(testName), Some(e), Some(duration), Some(formatter), None))
            reporter(TestFailed(tracker.nextOrdinal(), m, name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(testName),
              Some(f.exception), Some(duration), Some(formatter), None))

          case e @ Error(m, st) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            reporter(TestFailed(tracker.nextOrdinal(), m, name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(name),
              Some(e.exception), Some(duration), Some(formatter), None))
          case DecoratedResult(_, f @ Failure(m, e, st, d)) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            reporter(TestFailed(tracker.nextOrdinal(), m, name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(name),
              Some(f.exception), Some(duration), Some(formatter), None))

          case DecoratedResult(_, e @ Error(m, st)) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            reporter(TestFailed(tracker.nextOrdinal(), m, name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(name),
              Some(e.exception), Some(duration), Some(formatter), None))

          case Pending(_) | Skipped(_, _) =>
            //      notifier.fireTestIgnored(desc)
            reporter(TestPending(tracker.nextOrdinal(), name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(testName), None, Some(formatter)))

          case Success(_, _) | DecoratedResult(_, _) =>
            reporter(TestSucceeded(tracker.nextOrdinal(), name, id, Some(id),
              getDecodedName(name), testName, exampleName, getDecodedName(testName), Some(duration), Some(formatter), None))
        }
        //notifier.fireTestFinished(desc)
        //reporter()
      }
      case ExecutedSpecStart(_, _, _) => //notifier.fireTestRunStarted(desc) // TODO: Should we care?
      case ExecutedSpecEnd(_, _, _) => //notifier.fireTestRunFinished(new org.junit.runner.Result)
      case _ => // don't do anything otherwise too many tests will be counted
    }
  }

}
