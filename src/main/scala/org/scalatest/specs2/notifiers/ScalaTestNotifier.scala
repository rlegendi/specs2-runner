package org.scalatest.specs2.notifiers

import ScalaTestNotifier.decodedSuiteNameFor
import ScalaTestNotifier.getDecodedName
import ScalaTestNotifier.loc
import ScalaTestNotifier.rerunnerFor
import ScalaTestNotifier.suiteClassNameFor
import org.scalatest.Reporter
import org.scalatest.Suite
import org.scalatest.Tracker
import org.scalatest.events.InfoProvided
import org.scalatest.events.LineInFile
import org.scalatest.events.Location
import org.scalatest.events.MotionToSuppress
import org.scalatest.events.NameInfo
import org.scalatest.events.ScopeClosed
import org.scalatest.events.ScopeOpened
import org.scalatest.events.TestFailed
import org.scalatest.events.TestIgnored
import org.scalatest.events.TestNameInfo
import org.scalatest.events.TestPending
import org.scalatest.events.TestStarting
import org.scalatest.events.TestSucceeded
import org.specs2.execute.Details
import org.specs2.execute.FailureDetails
import org.specs2.main.Arguments
import org.specs2.reporter.Notifier
import org.specs2.specification.SpecificationStructure
import scala.collection.mutable.Stack
import scala.reflect.NameTransformer
import org.scalatest.specs2.Utils._

object ScalaTestNotifier {

  val debug = false

  // A sample Spec2 Location string looks something like:
  // 		classname (file name:line number)
  //
  // e.g.:
  // 		org.scalatest.specs2.Spec2Runner (Spec2Runner.scala:164)
  def loc(spec2Loc: String): Option[Location] = {
    require(spec2Loc != null)

    try {
      // Class name shouldn't contain these, so hopefully we find the opening
      val classNameEndIdx = spec2Loc.indexOf(" (")
      val className = spec2Loc.substring(0, classNameEndIdx)

      val colonIdx = spec2Loc.lastIndexOf(":")
      val file = spec2Loc.substring(classNameEndIdx + 2 /* colon is skipped */ , colonIdx)
      val line = spec2Loc.substring(colonIdx + 1, spec2Loc.size - 1 /* last ')' is dropped */ ).toInt

      return Some(LineInFile(line, file))
    } catch {
      case _ => return None
    }
  }

  // The suiteNameFor and suiteIdFor functions are placed into a Utils object because they are needed elsewhere too

  def suiteClassNameFor(spec: SpecificationStructure): Option[String] = {
    Some(spec.getClass.getName)
  }

  def rerunnerFor(spec: SpecificationStructure): Option[String] = {
    suiteClassNameFor(spec)
  }

  private def decodedSuiteNameFor(spec: SpecificationStructure): Option[String] = {
    val name = spec.getClass.getSimpleName
    getDecodedName(name)
  }

  private def getDecodedName(name: String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if (decoded == name) None else Some(decoded)
  }
}

/**
 * Simple utility class for handling the scope elements.
 *
 * @author rlegendi
 */
case class ScopeElement(val name: String, val location: Option[Location])

import ScalaTestNotifier._

/**
 * <b>Note</b> This implementation <b>is not thread-safe</b>! It depends on a scope stack
 * which should not be used concurrently.
 *
 * @author rlegendi
 */
class ScalaTestNotifier(val spec: SpecificationStructure, val args: Arguments,
  val tracker: Tracker, val reporter: Reporter) extends Notifier {

  // --------------------------------------------------------------------------
  // --- Members --------------------------------------------------------------

  val suiteStack: Stack[ScopeElement] = Stack()

  private var indentLevel: Int = 0

  // --------------------------------------------------------------------------
  // --- Scope handling -------------------------------------------------------

  def scopeOpened(name: String, location: String): Unit = {
    // TODO Requires... in the whole file
    suiteStack.push(new ScopeElement(name, loc(location)))
    // Ignore the first scope, which is same as the suiteName
    if (1 == suiteStack.size) {
      return
    }

    val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
    reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
      None, None, Some(formatter), loc(location)))

    indentLevel += 1
  }

  def scopeClosed(name: String, location: String): Unit = {
    suiteStack.pop
    // Closing the top-level scope would terminate the run, interrupting the execution of
    // multiple specs in the same file
    // TODO But needs to be verified :-)
    if (0 == suiteStack.size) {
      return
    }

    val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
    reporter(ScopeClosed(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
      None, None, Some(MotionToSuppress)))

    indentLevel -= 1
  }

  def specStart(title: String, location: String): Unit = {
    if (debug) {
      println(">>> specStart: " + title + "@" + location)
    }

    scopeOpened(title, location)
  }

  def specEnd(title: String, location: String): Unit = {
    if (debug) {
      println(">>> specEnd: " + title + "@" + location)
    }

    scopeClosed(title, location)
  }

  def contextStart(text: String, location: String): Unit = {
    if (debug) {
      println(">>> contextStart: " + text + "@" + location)
    }

    scopeOpened(text, location)
  }

  def contextEnd(text: String, location: String): Unit = {
    if (debug) {
      println(">>> contextEnd: " + text + "@" + location)
    }
    scopeClosed(text, location);
  }

  // --------------------------------------------------------------------------
  // --- Specific events ------------------------------------------------------

  def text(text: String, location: String): Unit = {
    if (debug) {
      println(">>> text: " + text + "@" + location)
    }

    val formatter = Suite.getIndentedText(text, indentLevel, true) // getIndentedTextForInfo with infoIsInsideATest = 1
    reporter(InfoProvided(
      ordinal = tracker.nextOrdinal(),
      message = text,
      nameInfo = Some(NameInfo(
        suiteName = suiteStack.head.name,
        suiteID = suiteIdFor(spec),
        suiteClassName = suiteClassNameFor(spec),
        decodedSuiteName = decodedSuiteNameFor(spec),
        testName = getNameInfo())),
      aboutAPendingTest = None, // Option[Boolean]
      aboutACanceledTest = None, // Option[Boolean]
      throwable = None, // Option[Throwable]
      formatter = Some(formatter),
      location = loc(location)))
  }

  def getNameInfo(): Option[TestNameInfo] = {
    /*
    suiteStack.head match {
      case s @ TestScope(_, testName) => Some(TestNameInfo(testName, getDecodedName(testName)))
      case s @ SuiteScope(_) => None
      case _ => None
    }
    */
    None
  }

  private def testStarted(name: String, location: String): Unit = {
    suiteStack.push(new ScopeElement(name, loc(location)))
  }

  private def testEnded(): ScopeElement = {
    suiteStack.pop
  }

  /**
   * This is required because two examples might have the same name, e.g.:
   *
   * <code>
   * class SameNamesSpecs extends Specification {
   * 	"One" should {
   * 		"a" in {
   * 			success
   * 		}
   * 	}
   *
   * 	"Other" should {
   * 		"a" in {
   * 			success
   * 		}
   * 	}
   * }
   * </code>
   *
   * In this case, <code>"a"</code> reported correctly, but the line numbers
   * are messed up (since the latter rewrites the previous object with the
   * same name).
   *
   * @param name the name of the test
   * @return a simple list containing a textual representation of the path
   */
  private def testNameFor(name: String): String = {
    suiteStack.foldLeft(name + ": ")((cum, act) => cum + ", " +
      act.name + "@" + act.location)
  }

  def exampleStarted(name: String, location: String): Unit = {
    if (debug) {
      println(">>> exampleStarted: " + name + "@" + location)
    }

    testStarted(name, location)
    val testName = testNameFor(name) // Must be evaluated *after* the test started

    reporter(TestStarting(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = name,
      decodedTestName = getDecodedName(name),
      formatter = Some(MotionToSuppress), // Note suppressed event - this is what we want here! See Scaladoc of Formatter
      location = loc(location),
      rerunner = rerunnerFor(spec)))
  }

  def exampleSuccess(name: String, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleSuccess: " + name + ", t=" + duration)
    }

    val testName = testNameFor(name)
    val actScopeElement = testEnded()

    val formatter = Suite.getIndentedText(name, indentLevel, true)
    reporter(TestSucceeded(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = name,
      decodedTestName = getDecodedName(name),
      duration = Some(duration),
      formatter = Some(formatter),
      location = actScopeElement.location))
  }

  private def testFailed(name: String, message: String, location: String, f: Throwable, details: Option[Details], duration: Long): Unit = {
    if (debug) {
      println(">>> testFailed: " + name + ", " + message + ", " + location + ", " + f + ", " + details + ", " + duration)
    }

    // We do not use the actual test location, because a test might contain multiple assertions, and any of them might fail
    // It is more specific if we can report the exact example which is erroreous
    val testName = testNameFor(name)
    testEnded()

    val formatter = Suite.getIndentedText(name, indentLevel, true)

    // Any better way to do this? What if a new Details subclass is introduced?
    // And its a bit against readability, is there anyway how I can make it a bit cleaner?
    val reason = details.getOrElse(name + "testText") match {
      case FailureDetails(expected, actual) if (args.diffs.show(expected, actual)) => {
        val (expectedDiff, actualDiff) = args.diffs.showDiffs(expected, actual)
        var ret = "Expected: " + expectedDiff + ", Actual: " + actualDiff
        if (args.diffs.showFull) {
          ret += EoL
          ret += "Expected (full): " + expected + EoL
          ret += "Actual (full):   " + actual
        }
        ret
      }
      case _ => ""
    }

    reporter(TestFailed(
      ordinal = tracker.nextOrdinal(),
      message = message,
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = name + ": " + reason,
      decodedTestName = Some(name),
      throwable = Some(f),
      duration = Some(duration),
      formatter = Some(formatter),
      location = loc(location),
      rerunner = rerunnerFor(spec)))
  }

  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleFailure: " + name + ", t=" + message)
    }

    testFailed(name, message, location, f, Some(details), duration)
  }

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleError: " + name + ", t=" + message)
    }

    // TODO Is there any way to report a test error without a suite error? Do I even need it?
    testFailed(name, message, location, f, None, duration)
  }

  // Note 1: duration cannot be reported in the standard ways through TestIgnored
  def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleSkipped: " + name + ", t=" + message)
    }

    val testName = testNameFor(name)
    val actScopeElement = testEnded()
    val formatter = Suite.getIndentedText(name, indentLevel, true)

    reporter(TestIgnored(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = name + ": " + message,
      decodedTestName = Some(name),
      formatter = Some(formatter),
      location = actScopeElement.location))
  }

  def examplePending(name: String, message: String, duration: Long): Unit = {
    if (debug) {
      println(">>> examplePending: " + name + ", t=" + message)
    }

    val testName = testNameFor(name)
    val actScopeElement = testEnded()
    val formatter = Suite.getIndentedText(name, indentLevel, true)

    reporter(TestPending(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = name + ": " + message,
      decodedTestName = Some(name),
      duration = Some(duration),
      formatter = Some(formatter),
      location = actScopeElement.location))
  }
}
