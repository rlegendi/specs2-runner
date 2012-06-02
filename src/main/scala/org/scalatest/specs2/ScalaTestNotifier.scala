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
      val line = spec2Loc.substring(colonIdx + 1, spec2Loc.size - 1 /* last ) is dropped */ ).toInt

      return Some(LineInFile(line, file))
    } catch {
      case _ => return None
    }
  }
}

// TODO Other params could be val members too...
class ScalaTestNotifier(val spec: SpecificationStructure, val tracker: Tracker, val reporter: Reporter) extends Notifier {

  var indentLevel: Int = 0
  private val scopeStack: Stack[String] = Stack()

  // TODO Rename: reportSuiteStartingAndScopeOpened() -- or something like that
  def scopeOpened(name: String, location: String) {
    reporter(SuiteStarting(ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = Some(spec.getClass.getName),
      decodedSuiteName = Some(spec.getClass.getSimpleName), // TODO Check this
      formatter = Some(MotionToSuppress),
      location = loc(location),
      rerunner = Some(spec.getClass.getName)))

    indentLevel += 1
    if (scopeStack.isEmpty)
      scopeStack.push("") // Ignore the first scope, which is same as the suiteName
    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
      scopeStack.push(scopeStack.head + (if (scopeStack.length > 1) " " else "") + name)
    if (scopeStack.length > 1) {
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, Some(spec.getClass.getName), Some(name)),
        None, None, Some(formatter)))
    }
  }

  def scopeClosed(name: String, location: String): Unit = {
    scopeStack.pop()
    if (scopeStack.length > 0) { // No need to fire for the last scope, which is the one same as the suiteName 
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeClosed(tracker.nextOrdinal, name, NameInfo(name, Some(spec.getClass.getName), Some(name)),
        None, None, Some(MotionToSuppress))) // TODO MotionToSuppress
    }
    indentLevel -= 1

    reporter(SuiteCompleted(ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = Some(spec.getClass.getName),
      decodedSuiteName = getDecodedName(spec.getClass.getSimpleName),
      duration = None, // We would need this here, don't we?
      formatter = Some(MotionToSuppress),
      location = loc(location)) // Should I include it here? Save during exampleStarted()?
      )
  }

  def getTestName(testText: String) = {
    if (scopeStack.isEmpty)
      testText
    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
      scopeStack.head + (if (scopeStack.length > 1) " " else "") + testText
  }

  // TODO TestStarting?

  def specStart(title: String, location: String) = {
    //indentLevel += 1 // Sure?
    //reporter(SuiteStarting(tracker.nextOrdinal(), title, NameInfo(name, Some(spec.getClass.getName), Some(name), None, None))
    //reporter(SuiteStarting(tracker.nextOrdinal, title, NameInfo(title, Some(spec.getClass.getName), Some(title)),
    //                     None, None, Some(MotionToSuppress)))

    // Note: decodedSuiteName: in case the suite name is put between backticks.  None if it is same as suiteName.

    //        NameInfo(title, Some(spec.getClass.getName), Some(title)),
    //                         None, None, Some(MotionToSuppress)))

    // TODO ScopeOpened

    /*
    val formatter = Suite.getIndentedTextForInfo(title, indentLevel, includeIcon = false, infoIsInsideATest = false) // TODO 
    reporter(SuiteStarting(tracker.nextOrdinal, title, suiteIdFor(spec), Some(spec.getClass.getName), None, Some(formatter), loc(location))) // ToDoLocation :-)
    */

    scopeOpened(title, location)
  }

  def specEnd(title: String, location: String) = {
    // indent -= 1 // TODO Decrease
    //reporter(SuiteCompleted(tracker.nextOrdinal(), title, title, None, None))
    scopeClosed(title, location)
  }

  private def getDecodedName(name: String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if (decoded == name) None else Some(decoded)
  }

  def contextStart(text: String, location: String) = {
    scopeOpened(text, location)
    //reporter(SuiteStarting(tracker.nextOrdinal(), text, text, None, None, None, loc(location)))
  }

  def contextEnd(text: String, location: String) = reporter(SuiteCompleted(tracker.nextOrdinal(), text, text, None, None, None, None, loc(location)))

  def text(text: String, location: String) = reporter(InfoProvided(tracker.nextOrdinal(), text, None, None, None, None, None, loc(location)))

  def exampleStarted(name: String, location: String) = {
    val testName = getTestName(name)
    //reporter(TestStarting(tracker.nextOrdinal(), name, name, None, None, "", "", None, None, loc(location)))
    //reporter(TestStarting(tracker.nextOrdinal(), spec.getClass.getSimpleName, spec.getClass.getName, Some(spec.getClass.getName), getDecodedName(spec.getClass.getSimpleName), testName, exampleName, getDecodedName(testName), Some(MotionToSuppress), None, Some(spec.getClass.getName)))
    reporter(TestStarting(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = Some(spec.getClass.getName),
      decodedSuiteName = getDecodedName(spec.getClass.getSimpleName),
      testName = testName,
      testText = testName + "(exampleName)",
      decodedTestName = getDecodedName(testName),
      formatter = Some(MotionToSuppress), // Note suppressed event - this is what we want here! See Scaladoc of Formatter
      location = loc(location),
      rerunner = Some(spec.getClass.getName)))
  }

  def exampleSuccess(name: String, duration: Long) = {
    val formatter = Suite.getIndentedText(name, indentLevel + 1, true)
    val testName = getTestName(name)
    reporter(TestSucceeded(
      ordinal = tracker.nextOrdinal(),
      suiteName = suiteNameFor(spec),
      suiteId = suiteIdFor(spec),
      suiteClassName = Some(spec.getClass.getName),
      decodedSuiteName = getDecodedName(spec.getClass.getSimpleName),
      testName = testName,
      testText = testName + "(exampleName)",
      decodedTestName = getDecodedName(testName),
      duration = Some(duration),
      formatter = Some(formatter),
      location = None) // Should I include it here? Save during exampleStarted()?
      )
  }

  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None, None, None, None, loc(location)))

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None, None, None, None, loc(location)))

  def exampleSkipped(name: String, message: String, duration: Long) =
    reporter(TestIgnored(tracker.nextOrdinal(), message, "", None, None, "", "", None))

  def examplePending(name: String, message: String, duration: Long) =
    reporter(TestPending(tracker.nextOrdinal(), message, "", None, None, "", "", None))
}
