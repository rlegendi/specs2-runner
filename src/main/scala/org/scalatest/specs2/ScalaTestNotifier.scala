package org.scalatest.specs2

import org.specs2.reporter.Notifier
import org.specs2.execute.Details
import org.scalatest.Reporter
import org.scalatest.Tracker
import org.scalatest.events.InfoProvided
import org.scalatest.events.TestSucceeded
import org.scalatest.events.TestStarting
import org.scalatest.events.SuiteStarting
import org.scalatest.events.TestPending
import org.scalatest.events.TestFailed
import org.scalatest.events.SuiteCompleted
import org.scalatest.Suite
import org.scalatest.events.NameInfo
import org.specs2.specification.SpecificationStructure
import Utils._
import org.scalatest.events.ToDoLocation
import org.scalatest.events.LineInFile
import org.scalatest.events.Location
import org.scalatest.events.LineInFile
import org.scalatest.events.LineInFile
import ScalaTestNotifier._
import scala.collection.mutable.Stack
import org.scalatest.specs.ScalaTestAbstractNotifier
import org.scalatest.specs.ScalaTestAbstractNotifier
import org.scalatest.events.ScopeOpened
import org.scalatest.events.ScopeClosed
import org.scalatest.events.MotionToSuppress
import scala.reflect.NameTransformer
import org.scalatest.events.TestIgnored
import org.specs2.execute.FailureDetails
import org.specs2.main.Arguments
import org.specs2.specification.SpecificationStructure
import org.specs2.specification.SpecificationStructure

object ScalaTestNotifier {

  // A sample Spec2 Location string looks something like:
  //
  // org.scalatest.specs2.Spec2Runner (Spec2Runner.scala:164)
  // classname (file name:line number)
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

  private def getDecodedSuiteNameFor(spec: SpecificationStructure): Option[String] = {
    val name = spec.getClass.getSimpleName
    getDecodedName(name)
  }

  private def getDecodedName(name: String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if (decoded == name) None else Some(decoded)
  }
}

// TODO Other params could be val members too...
class ScalaTestNotifier(val spec: SpecificationStructure, val args: Arguments, val tracker: Tracker, val reporter: Reporter) extends Notifier {

  var indentLevel: Int = 0
  private val scopeStack: Stack[String] = Stack()

  // TODO Rename: reportSuiteStartingAndScopeOpened() -- or something like that
  def scopeOpened(name: String, location: String): Unit = {
    reporter(SuiteStarting(ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = Some(spec.getClass.getSimpleName), // TODO Check this
      formatter = Some(MotionToSuppress),
      location = loc(location),
      rerunner = rerunnerFor(spec)))

    indentLevel += 1
    if (scopeStack.isEmpty)
      scopeStack.push("") // Ignore the first scope, which is same as the suiteName
    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
      scopeStack.push(scopeStack.head + (if (scopeStack.length > 1) " " else "") + name)
    if (scopeStack.length > 1) {
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
        None, None, Some(formatter)))
    }
  }

  def scopeClosed(name: String, location: String): Unit = {
    scopeStack.pop()
    if (scopeStack.length > 0) { // No need to fire for the last scope, which is the one same as the suiteName 
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeClosed(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
        None, None, Some(MotionToSuppress))) // TODO MotionToSuppress
    }
    indentLevel -= 1

    reporter(SuiteCompleted(ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = getDecodedSuiteNameFor(spec),
      duration = None, // We would need this here, don't we?
      formatter = Some(MotionToSuppress),
      location = loc(location)) // Should I include it here? Save during exampleStarted()?
      )
  }

  def getTestName(testText: String): String = {
    if (scopeStack.isEmpty)
      testText
    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
      scopeStack.head + (if (scopeStack.length > 1) " " else "") + testText
  }

  // TODO TestStarting?

  def specStart(title: String, location: String): Unit = {
    //indentLevel += 1 // Sure?
    //reporter(SuiteStarting(tracker.nextOrdinal(), title, NameInfo(name, suiteClassNameFor(spec), Some(name), None, None))
    //reporter(SuiteStarting(tracker.nextOrdinal, title, NameInfo(title, suiteClassNameFor(spec), Some(title)),
    //                     None, None, Some(MotionToSuppress)))

    // Note: decodedSuiteName: in case the suite name is put between backticks.  None if it is same as suiteName.

    //        NameInfo(title, suiteClassNameFor(spec), Some(title)),
    //                         None, None, Some(MotionToSuppress)))

    // TODO ScopeOpened

    /*
    val formatter = Suite.getIndentedTextForInfo(title, indentLevel, includeIcon = false, infoIsInsideATest = false) // TODO 
    reporter(SuiteStarting(tracker.nextOrdinal, title, suiteIdFor(spec), suiteClassNameFor(spec), None, Some(formatter), loc(location))) // ToDoLocation :-)
    */

    scopeOpened(title, location)
  }

  def specEnd(title: String, location: String): Unit = {
    // indent -= 1 // TODO Decrease
    //reporter(SuiteCompleted(tracker.nextOrdinal(), title, title, None, None))
    scopeClosed(title, location)
  }

  def contextStart(text: String, location: String): Unit = {
    scopeOpened(text, location)
    //reporter(SuiteStarting(tracker.nextOrdinal(), text, text, None, None, None, loc(location)))
  }

  def contextEnd(text: String, location: String): Unit = {
    reporter(SuiteCompleted(tracker.nextOrdinal(), text, text, None, None, None, None, loc(location)))
  }

  def text(text: String, location: String): Unit = {
    reporter(InfoProvided(tracker.nextOrdinal(), text, None, None, None, None, None, loc(location)))
  }

  def exampleStarted(name: String, location: String): Unit = {
    val testName = getTestName(name)
    reporter(TestStarting(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = getDecodedSuiteNameFor(spec),
      testName = testName,
      testText = testName + "(exampleName)",
      decodedTestName = getDecodedName(testName),
      formatter = Some(MotionToSuppress), // Note suppressed event - this is what we want here! See Scaladoc of Formatter
      location = loc(location),
      rerunner = rerunnerFor(spec)))
  }

  // Note: we could report the location on the other hand, but it is not accessible here
  def exampleSuccess(name: String, duration: Long): Unit = {
    val formatter = Suite.getIndentedText(name, indentLevel + 1, true)
    val testName = getTestName(name)
    reporter(TestSucceeded(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = getDecodedSuiteNameFor(spec),
      testName = testName,
      testText = testName + "(testText)", // TODO Check where it is used
      decodedTestName = getDecodedName(testName),
      duration = Some(duration),
      formatter = Some(formatter),
      location = None)) // Should I include it here? Save during exampleStarted()?
  }

  private def testFailed(name: String, message: String, location: String, f: Throwable, details: Option[Details], duration: Long): Unit = {
    val formatter = Suite.getIndentedText(name, indentLevel + 1, true)

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
      decodedSuiteName = getDecodedSuiteNameFor(spec),
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
    testFailed(name, message, location, f, Some(details), duration)
  }

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit = {
    // TODO Is there any way to report a test error without a suite error? Do I even need it?
    testFailed(name, message, location, f, None, duration)
  }

  // Note 1: duration cannot be reported in the standard ways through TestIgnored
  // Note 2: we could report the location on the other hand, but it is not accessible here
  def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    val formatter = Suite.getIndentedText(name, indentLevel + 1, true)

    reporter(TestIgnored(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = getDecodedSuiteNameFor(spec),
      testName = name,
      testText = message,
      decodedTestName = Some(name),
      formatter = Some(formatter),
      location = None)) // See Note 2
  }

  // Note: we could report the location on the other hand, but it is not accessible here
  def examplePending(name: String, message: String, duration: Long): Unit = {
    val formatter = Suite.getIndentedText(name, indentLevel + 1, true)

    reporter(TestPending(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = suiteClassNameFor(spec),
      decodedSuiteName = getDecodedSuiteNameFor(spec),
      testName = name,
      testText = message,
      decodedTestName = Some(name),
      duration = Some(duration),
      formatter = Some(formatter),
      location = None)) // See Note
  }
}
