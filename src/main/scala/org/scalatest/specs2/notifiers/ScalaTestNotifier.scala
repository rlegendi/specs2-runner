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

case class ScopeElement(val name: String, val location: Option[Location])

class ScalaTestNotifier(val spec: SpecificationStructure, val args: Arguments, val tracker: Tracker, val reporter: Reporter) extends Notifier {
  private val debug = false
  private val suiteStack: Stack[ScopeElement] = Stack()

  private var indentLevel: Int = 0

  def scopeOpened(name: String, location: String): Unit = {
    // TODO Requires... in the whole file
    suiteStack.push(new ScopeElement(name, loc(location)))
    // Ignore the first scope, which is same as the suiteName
    if (1 == suiteStack.size) {
      return
    }

    val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
    reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
      None, None, Some(formatter)))

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

  // TODO Externalize! (i.e., title == spec.name or something else...)
  var firstSpec = true

  def specStart(title: String, location: String): Unit = {
    if (debug) {
      println(">>> specStart: " + title + "@" + location)
    }

    //val formatter = Suite.getIndentedText(title, indentLevel, true)
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
        testName = getNameInfo())), // : Option[NameInfo],
      aboutAPendingTest = None, // : Option[Boolean] = None,
      aboutACanceledTest = None, // : Option[Boolean] = None,
      throwable = None, // : Option[Throwable] = None,
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

  def exampleStarted(name: String, location: String): Unit = {
    if (debug) {
      println(">>> exampleStarted: " + name + "@" + location)
    }

    //val testName = getTestName(name)
    val testName = name
    reporter(TestStarting(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = testName + "(exampleName)",
      decodedTestName = getDecodedName(testName),
      formatter = Some(MotionToSuppress), // Note suppressed event - this is what we want here! See Scaladoc of Formatter
      location = loc(location),
      rerunner = rerunnerFor(spec)))
  }

  // Note: we could report the location on the other hand, but it is not accessible here
  def exampleSuccess(name: String, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleSuccess: " + name + ", t=" + duration)
    }

    val formatter = Suite.getIndentedText(name, indentLevel, true)
    //val testName = getTestName(name)
    val testName = name
    reporter(TestSucceeded(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = testName,
      testText = testName + "(testText)", // TODO Check where it is used
      decodedTestName = getDecodedName(testName),
      duration = Some(duration),
      formatter = Some(formatter),
      location = None)) // Should I include it here? Save during exampleStarted()?
  }

  private def testFailed(name: String, message: String, location: String, f: Throwable, details: Option[Details], duration: Long): Unit = {
    if (debug) {
      println(">>> testFailed: " + name + ", " + message + ", " + location + ", " + f + ", " + details + ", " + duration)
    }

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
      testName = name,
      testText = reason,
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
  // Note 2: we could report the location on the other hand, but it is not accessible here
  def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    if (debug) {
      println(">>> exampleSkipped: " + name + ", t=" + message)
    }

    val formatter = Suite.getIndentedText(name, indentLevel, true)

    reporter(TestIgnored(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = name,
      testText = message,
      decodedTestName = Some(name),
      formatter = Some(formatter),
      location = None)) // See Note 2
  }

  // Note: we could report the location on the other hand, but it is not accessible here
  def examplePending(name: String, message: String, duration: Long): Unit = {
    if (debug) {
      println(">>> examplePending: " + name + ", t=" + message)
    }

    val formatter = Suite.getIndentedText(name, indentLevel, true)

    reporter(TestPending(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = decodedSuiteNameFor(spec),
      testName = name,
      testText = message,
      decodedTestName = Some(name),
      duration = Some(duration),
      formatter = Some(formatter),
      location = None)) // See Note
  }
}
