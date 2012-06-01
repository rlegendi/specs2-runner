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

// TODO Isn't this discouraged?
import ScalaTestNotifier._

// TODO Other params could be val members too...
class ScalaTestNotifier(val spec: SpecificationStructure, tracker: Tracker, reporter: Reporter) extends Notifier {

  //val spec = theSpec;

  var indent: Int = 0

  // TODO TestStarting?

  def specStart(title: String, location: String) = {
    indent += 1
    val formatter = Suite.getIndentedTextForInfo(title, indent, includeIcon = false, infoIsInsideATest = false) // TODO 
    //reporter(SuiteStarting(tracker.nextOrdinal(), title, NameInfo(name, Some(spec.getClass.getName), Some(name), None, None))
    //reporter(SuiteStarting(tracker.nextOrdinal, title, NameInfo(title, Some(spec.getClass.getName), Some(title)),
    //                     None, None, Some(MotionToSuppress)))

    // Note: decodedSuiteName: in case the suite name is put between backticks.  None if it is same as suiteName.

    reporter(SuiteStarting(tracker.nextOrdinal, title, suiteIdFor(spec), Some(spec.getClass.getName), None, Some(formatter), loc(location))) // ToDoLocation :-) 
    //        NameInfo(title, Some(spec.getClass.getName), Some(title)),
    //                         None, None, Some(MotionToSuppress)))

    // TODO ScopeOpened
  }

  def specEnd(title: String, location: String) = {
    indent -= 1
    reporter(SuiteCompleted(tracker.nextOrdinal(), title, title, None, None))
  }

  def contextStart(text: String, location: String) = reporter(SuiteStarting(tracker.nextOrdinal(), text, text, None, None, None, loc(location)))

  def contextEnd(text: String, location: String) = reporter(SuiteCompleted(tracker.nextOrdinal(), text, text, None, None, None, None, loc(location)))

  def text(text: String, location: String) = reporter(InfoProvided(tracker.nextOrdinal(), text, None, None, None, None, None, loc(location)))

  def exampleStarted(name: String, location: String) = {
    reporter(TestStarting(tracker.nextOrdinal(), name, name, None, None, "", "", None, None, loc(location)))
  }

  def exampleSuccess(name: String, duration: Long) = reporter(TestSucceeded(tracker.nextOrdinal(), name, name, None, None, "", "", None))

  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None, None, None, None, loc(location)))

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None, None, None, None, loc(location)))

  def exampleSkipped(name: String, message: String, duration: Long) =
    reporter(TestPending(tracker.nextOrdinal(), message, "", None, None, "", "", None))

  def examplePending(name: String, message: String, duration: Long) =
    reporter(TestPending(tracker.nextOrdinal(), message, "", None, None, "", "", None))
}
