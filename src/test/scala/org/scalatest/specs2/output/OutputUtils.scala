package org.scalatest.specs2.output

import org.scalatest.events.RunCompleted
import org.scalatest.events.InfoProvided
import org.scalatest.events.ScopeOpened
import org.scalatest.events.TestSucceeded
import org.scalatest.events.RunAborted
import org.scalatest.events.TestStarting
import org.scalatest.events.SuiteStarting
import org.scalatest.events.TestIgnored
import org.scalatest.events.TestPending
import org.scalatest.events.IndentedText
import org.scalatest.events.SuiteAborted
import org.scalatest.events.TestFailed
import org.scalatest.events.SuiteCompleted
import org.scalatest.events.RunStopped
import org.scalatest.events.TestCanceled
import org.scalatest.Distributor
import org.scalatest.events.ScopeClosed
import org.scalatest.specs2.Spec2Runner
import org.specs2.specification.SpecificationStructure
import org.scalatest.events.RunStarting
import org.scalatest.Stopper
import org.scalatest.events.MarkupProvided
import org.scalatest.events.Event
import scala.collection.mutable.Stack
import org.scalatest.Reporter
import org.scalatest.Filter
import org.scalatest.Tracker
import org.scalatest.events.Ordinal

// TODO Title?
// TODO Other things from here http://etorreborre.github.com/specs2/guide/org.specs2.guide.Structure.html#Unit+specifications

case class TestOutput(lvl: Int, msg: String)

class TestReporter extends Reporter {
  val stack: Stack[TestOutput] = Stack()

  var testStartedCtr = 0
  var suiteAbortedCtr = 0
  var suiteCompletedCtr = 0
  var testIgnoredCtr = 0
  var infoProvidedCtr = 0
  var markupProvidedCtr = 0
  var scopeOpenedCtr = 0
  var scopeClosedCtr = 0
  var testPendingCtr = 0
  var testCanceledCtr = 0
  var runStartingCtr = 0
  var runCompletedCtr = 0
  var runStoppedCtr = 0
  var runAbortedCtr = 0
  var testSucceededCtr = 0
  var testFailedCtr = 0
  var suiteStartingCtr = 0

  def apply(event: Event) {
    event match {
      case e: TestStarting => testStartedCtr += 1
      case e: SuiteAborted => suiteAbortedCtr += 1
      case e: SuiteCompleted => suiteCompletedCtr += 1
      case e: TestIgnored => testIgnoredCtr += 1
      case e: InfoProvided => infoProvidedCtr += 1
      case e: MarkupProvided => markupProvidedCtr += 1
      case e @ ScopeOpened(_, name, _, _, _, Some(formatter), _, _, _, _) => {
        formatter match {
          case ft @ IndentedText(formattedText, rawText, indentationLevel) => {
            stack.push(TestOutput(indentationLevel, rawText))
          }
          case _ => ()
        }
        scopeOpenedCtr += 1
      }
      case e: ScopeClosed => scopeClosedCtr += 1
      case e: TestPending => testPendingCtr += 1
      case e: TestCanceled => testCanceledCtr += 1
      case e: RunStarting => runStartingCtr += 1
      case e: RunCompleted => runCompletedCtr += 1
      case e: RunStopped => runStoppedCtr += 1
      case e: RunAborted => runAbortedCtr += 1
      case e @ TestSucceeded(_, suiteName, _, _, _, testName, testText, _, _, Some(formatter), location, _, _, _, _) => {
        formatter match {
          case ft @ IndentedText(formattedText, rawText, indentationLevel) => {
            stack.push(TestOutput(indentationLevel, rawText))
          }
          case _ => ()
        }
        testSucceededCtr += 1
      }
      case e: TestFailed => testFailedCtr += 1
      case e: SuiteStarting => suiteStartingCtr += 1
      case _ => ()
    }
  }
}

object OutputUtils {
  // Had to hide this to prevent continuous OutOfMemory (PermGenSpace) caused by sbt-scct
  class DummySpec2Runner(specs2Class: Class[_ <: SpecificationStructure]) extends Spec2Runner(specs2Class) {
    override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {

      // Note that reporter is unused
      val usedReporter = new TestReporter()
      super.run(testName, usedReporter, stopper, filter, configMap, distributor, tracker)
    }
  }

  def runSpecAndReturnReversedScopeStack(clazz: Class[_ <: SpecificationStructure]) = {
    val defaultRunstamp = 1
    val tracker = new Tracker(new Ordinal(defaultRunstamp))
    val reporter = new TestReporter
    val runner = new DummySpec2Runner(clazz)

    runner.runSpec2(tracker, reporter, Filter())
    reporter.stack.reverse.foreach(ft => println(ft.lvl + "/" + ft.msg))
    reporter.stack.reverse
  }
}
