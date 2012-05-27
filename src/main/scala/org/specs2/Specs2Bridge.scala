package org.specs2

import org.specs2.reflect.Classes
import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure
import org.specs2.main.Arguments
import org.specs2.specification.ExecutingSpecification
import org.specs2.specification.ExecutingFragment
import org.specs2.specification.SpecName
import org.specs2.specification.ExecutedFragment
import org.specs2.execute.Pending
import org.specs2.specification.ExecutedResult
import org.specs2.execute.DecoratedResult
import org.specs2.execute.Skipped
import org.scalatest.Reporter
import org.specs2.execute.Failure
import org.specs2.execute.Success
import org.scalatest.Tracker
import org.scalatest.events.TestStarting
import scala.reflect.NameTransformer
import org.scalatest.events.MotionToSuppress
import org.specs2.specification.ExecutedSpecStart
import org.specs2.specification.ExecutedSpecEnd
import org.scalatest.events.TestSucceeded
import org.scalatest.events.TestSucceeded$
import org.scalatest.events.TestSucceeded
import org.scalatest.Suite
import org.scalatest.ScalaTestBridge
import org.scalatest.events.TestFailed
import org.specs2.execute.Error
import org.scalatest.events.TestPending

object Specs2Bridge {
  def tryToCreateObject[T <: AnyRef](className: String, printMessage: Boolean = true, printStackTrace: Boolean = true,
    loader: ClassLoader = Thread.currentThread.getContextClassLoader)(implicit m: Manifest[T]): Option[T] = {

    Classes.tryToCreateObject(className, printMessage, printStackTrace, loader)(m)
  }

  def getContentFor(spec: SpecificationStructure): Fragments = spec.content;

  def createExecuteSpecification(name: SpecName, fs: Seq[ExecutedFragment], args: Arguments = Arguments()): ExecutingSpecification =
    ExecutingSpecification.create(name, fs, args)

  def parseArguments(arguments: Seq[String]): Arguments = {
    Arguments(arguments: _*) // TODO Hm, this is kinda smelly here
  }

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
        val duration = 1000 //System.currentTimeMillis() - exampleStart
        //val formatter = ScalaTestBridge.getIndentedText(exampleName, indentLevel + 1, true)
        val formatter = ScalaTestBridge.getIndentedText(testName, 2, true)

        reporter(TestStarting(tracker.nextOrdinal(), spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
          getDecodedName(spec2.getClass.getSimpleName), testName, testName,
          getDecodedName(testName), Some(MotionToSuppress), None, Some(spec2.getClass.getName)))

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
          
          // TODO Indenting?
          
        result match {
          case f @ Failure(m, e, st, d) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            //reporter(TestFailed(tracker.nextOrdinal(), e.getMessage, spec.getClass.getSimpleName, spec.getClass.getName, Some(spec.getClass.getName), getDecodedName(spec.getClass.getSimpleName), testName, exampleName, getDecodedName(testName), Some(e), Some(duration), Some(formatter), None))
            reporter(TestFailed(tracker.nextOrdinal(), m, spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName + " (exampleName)", getDecodedName(testName),
              Some(f.exception), Some(duration), Some(formatter), None))

          case e @ Error(m, st) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            reporter(TestFailed(tracker.nextOrdinal(), m, spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName + " (exampleName)", getDecodedName(spec2.getClass.getSimpleName),
              Some(e.exception), Some(duration), Some(formatter), None))
          case DecoratedResult(_, f @ Failure(m, e, st, d)) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            reporter(TestFailed(tracker.nextOrdinal(), m, spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName + " (exampleName)", getDecodedName(spec2.getClass.getSimpleName),
              Some(f.exception), Some(duration), Some(formatter), None))

          case DecoratedResult(_, e @ Error(m, st)) =>
            //              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            reporter(TestFailed(tracker.nextOrdinal(), m, spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName + " (exampleName)", getDecodedName(spec2.getClass.getSimpleName),
              Some(e.exception), Some(duration), Some(formatter), None))

          case Pending(_) | Skipped(_, _) =>
            //      notifier.fireTestIgnored(desc)
            reporter(TestPending(tracker.nextOrdinal(), spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName + " (exampleName)", getDecodedName(testName), None, Some(formatter)))

          case Success(_, _) | DecoratedResult(_, _) =>
            reporter(TestSucceeded(tracker.nextOrdinal(), spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
              getDecodedName(spec2.getClass.getSimpleName), testName, testName /*exampleName*/ , getDecodedName(testName), Some(duration), Some(formatter), None))
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
