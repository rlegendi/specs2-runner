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
import org.scalatest.events.Formatter
import org.scalatest.events.NameInfo
import org.scalatest.events.TestNameInfo
import org.scalatest.events.NameInfo$
import scala.reflect.generic.Scopes

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

  private def decodedSuiteNameFor(spec: SpecificationStructure): Option[String] = {
    val name = spec.getClass.getSimpleName
    getDecodedName(name)
  }

  private def getDecodedName(name: String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if (decoded == name) None else Some(decoded)
  }
}

case class SuiteScope(val suiteName: String)

// Override here?
case class TestScope(override val suiteName: String, val testName: String) extends SuiteScope(suiteName)

// TODO Other params could be val members too...
class ScalaTestNotifier(val spec: SpecificationStructure, val args: Arguments, val tracker: Tracker, val reporter: Reporter) extends Notifier {

  val debug = false
  var indentLevel: Int = 0
  private val suiteStack: Stack[SuiteScope] = Stack()

  // TODO Rename: reportSuiteStartingAndScopeOpened() -- or something like that
  def scopeOpened(name: String, location: String): Unit = {
    //suiteStack.push(SuiteScope(name))
    //    reporter(SuiteStarting(ordinal = tracker.nextOrdinal(),
    //      suiteName = suiteNameFor(spec),
    //      suiteId = suiteIdFor(spec),
    //      suiteClassName = suiteClassNameFor(spec),
    //      decodedSuiteName = Some(spec.getClass.getSimpleName), // TODO Check this
    //      formatter = Some(MotionToSuppress),
    //      location = loc(location),
    //      rerunner = rerunnerFor(spec)))

    //    if (scopeStack.isEmpty)
    //      scopeStack.push("") // Ignore the first scope, which is same as the suiteName
    //    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
    //      scopeStack.push(scopeStack.head + (if (scopeStack.length > 1) " " else "") + name)
    //      //scopeStack.push(name)
    //    if (scopeStack.length > 1) {
    
    /*
	suiteStack.push(SuiteScope(name))    
    if (indentLevel > 0) {
    //if (!suiteStack.isEmpty) {
      if (debug) {
        println("Indent: " + indentLevel)
        println("Scope Opened: " + name)
      }
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
        None, None, Some(formatter)))
    }
    */
//   	indentLevel += 1
    
    suiteStack.push(SuiteScope(name))
//    if(1 == suiteStack.size) {
//      return
//    }
    
    if (debug) {
      println("Indent: " + indentLevel)
      println("Scope Opened: " + name)
    }
    val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
    reporter(ScopeOpened(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
      None, None, Some(formatter)))
      
    //suiteStack.push(SuiteScope(name))    
    indentLevel += 1
    //    }
  }

  def scopeClosed(name: String, location: String): Unit = {
    //    scopeStack.pop()
    //    if (scopeStack.length > 0) { // No need to fire for the last scope, which is the one same as the suiteName
    
    suiteStack.pop
//    if (0 == suiteStack.size) {
//      return
//    }
    
    // Closing the outmost scope would terminate the run, interrupting the execution of multiple specs in the same file
    // But needs to be verified :-)
    //if (indentLevel > 1) {
   	//suiteStack.pop
    //if (!suiteStack.isEmpty) {
      val formatter = Suite.getIndentedTextForInfo(name, indentLevel, false, false)
      reporter(ScopeClosed(tracker.nextOrdinal, name, NameInfo(name, suiteClassNameFor(spec), Some(name)),
        None, None, Some(MotionToSuppress))) // TODO MotionToSuppress
      //    }
    //}
    
    indentLevel -= 1
    //suiteStack.pop

    //    reporter(SuiteCompleted(ordinal = tracker.nextOrdinal(),
    //      suiteName = suiteNameFor(spec),
    //      suiteId = suiteIdFor(spec),
    //      suiteClassName = suiteClassNameFor(spec),
    //      decodedSuiteName = decodedSuiteNameFor(spec),
    //      duration = None, // We would need this here, don't we?
    //      formatter = Some(MotionToSuppress),
    //      location = loc(location)) // Should I include it here? Save during exampleStarted()?
    //      )
  }

  //  def getTestName(testText: String): String = {
  //    if (scopeStack.isEmpty)
  //      testText
  //    else // the scopeStack.length check is to make sure for the first scope "", there's no need for the space to concat.
  //      scopeStack.head + (if (scopeStack.length > 1) " " else "") + testText
  //  }

  // TODO TestStarting?

  // TODO Externalize! (i.e., title == spec.name or something else...)
  var firstSpec = true

  def specStart(title: String, location: String): Unit = {
    if (debug) {
      println(">>> specStart: " + title + "@" + location)
    }
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

    //val formatter = (indentLevel > 0) ? Suite.getIndentedText(title, indentLevel + 1, true)  : MotionToSuppress

    //val formatter = if (indentLevel > 0) Suite.getIndentedText(title, indentLevel + 1, true) else MotionToSuppress
    //    val formatter: Formatter = if (firstSpec) {
    //      firstSpec = false
    //	  MotionToSuppress
    //    } else {
    //      Suite.getIndentedText(title, indentLevel + 1, true) 
    //    }

    val formatter = Suite.getIndentedText(title, indentLevel, true)

    //    reporter(SuiteStarting(ordinal = tracker.nextOrdinal(),
    //      suiteName = suiteNameFor(spec),
    //      suiteId = suiteIdFor(spec),
    //      suiteClassName = suiteClassNameFor(spec),
    //      decodedSuiteName = Some(spec.getClass.getSimpleName), // TODO Check this
    //      formatter = Some(formatter),
    //      location = loc(location),
    //      rerunner = rerunnerFor(spec)))

//    if (0 == indentLevel) {
//      return
//    }
    
    scopeOpened(title, location)
  }

  def specEnd(title: String, location: String): Unit = {
    if (debug) {
      println(">>> specEnd: " + title + "@" + location)
    }
    // indent -= 1 // TODO Decrease
    //reporter(SuiteCompleted(tracker.nextOrdinal(), title, title, None, None))
    
//    if (0 == indentLevel ) {
//      return
//    }
    
    scopeClosed(title, location)
    
    //    reporter(SuiteCompleted(ordinal = tracker.nextOrdinal(),
    //      suiteName = suiteNameFor(spec),
    //      suiteId = suiteIdFor(spec),
    //      suiteClassName = suiteClassNameFor(spec),
    //      decodedSuiteName = decodedSuiteNameFor(spec),
    //      duration = None, // We would need this here, don't we?
    //      formatter = Some(MotionToSuppress),
    //      location = loc(location))) // Should I include it here? Save during exampleStarted()?
  }

  def contextStart(text: String, location: String): Unit = {
    if (debug) {
      println(">>> contextStart: " + text + "@" + location)
    }

    scopeOpened(text, location)
    //indentLevel += 1
    //reporter(SuiteStarting(tracker.nextOrdinal(), text, text, None, None, None, loc(location)))
  }

  def contextEnd(text: String, location: String): Unit = {
    if (debug) {
      println(">>> contextEnd: " + text + "@" + location)
    }

    scopeClosed(text, location);
   	//indentLevel -= 1
    //reporter(SuiteCompleted(tracker.nextOrdinal(), text, text, None, None, None, None, loc(location)))
  }

  def text(text: String, location: String): Unit = {
    if (debug) {
      println(">>> text: " + text + "@" + location)
    }

    val formatter = Suite.getIndentedText(text, indentLevel, true) // getIndentedTextForInfo with infoIsInsideATest = 1
    reporter(InfoProvided(
      ordinal = tracker.nextOrdinal(),
      message = text,
      //nameInfo = None, // : Option[NameInfo],
      nameInfo = Some(NameInfo(
        suiteName = suiteStack.head.suiteName,
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
    suiteStack.head match {
      case s @ TestScope(_, testName) => Some(TestNameInfo(testName, getDecodedName(testName)))
      case s @ SuiteScope(_) => None
      case _ => None
    }
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

class EmptyNotifier extends Notifier {
  def specStart(title: String, location: String): Unit = {
    println(">>> specStart: " + title + "@" + location)
  }

  def specEnd(title: String, location: String): Unit = {
    println(">>> specEnd: " + title + "@" + location)
  }

  def contextStart(text: String, location: String): Unit = {
    println(">>> contextStart: " + text + "@" + location)
  }

  def contextEnd(text: String, location: String): Unit = {
    println(">>> contextEnd: " + text + "@" + location)
  }

  def text(text: String, location: String): Unit = {
    println(">>> text: " + text + "@" + location)
  }

  def exampleStarted(name: String, location: String): Unit = {
    println(">>> exampleStarted: " + name + "@" + location)
  }

  def exampleSuccess(name: String, duration: Long): Unit = {
    println(">>> exampleSuccess: " + name + ", t=" + duration)
  }

  private def testFailed(name: String, message: String, location: String, f: Throwable, details: Option[Details], duration: Long): Unit = {
    println(">>> testFailed: " + name + ", " + message + ", " + location + ", " + f + ", " + details + ", " + duration)
  }

  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long): Unit = {
    println(">>> exampleFailure: " + name + ", t=" + message)
  }

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit = {
    println(">>> exampleError: " + name + ", t=" + message)
  }

  def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    println(">>> exampleSkipped: " + name + ", t=" + message)
  }

  def examplePending(name: String, message: String, duration: Long): Unit = {
    println(">>> examplePending: " + name + ", t=" + message)
  }

}