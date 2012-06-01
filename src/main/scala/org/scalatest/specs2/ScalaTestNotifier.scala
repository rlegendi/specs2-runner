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

class ScalaTestNotifier(tracker: Tracker, reporter: Reporter) extends Notifier {

  var indent: Int = 0

  def specStart(title: String, location: String) = reporter(SuiteStarting(tracker.nextOrdinal(), title, title, None, None))
  def specEnd(title: String, location: String) = reporter(SuiteCompleted(tracker.nextOrdinal(), title, title, None, None))
  def contextStart(text: String, location: String) = reporter(SuiteStarting(tracker.nextOrdinal(), text, text, None, None))
  def contextEnd(text: String, location: String) = reporter(SuiteCompleted(tracker.nextOrdinal(), text, text, None, None))
  def text(text: String, location: String) = reporter(InfoProvided(tracker.nextOrdinal(), text, None))
  def exampleStarted(name: String, location: String) = reporter(TestStarting(tracker.nextOrdinal(), name, name, None, None, "", "", None))
  def exampleSuccess(name: String, duration: Long) = reporter(TestSucceeded(tracker.nextOrdinal(), name, name, None, None, "", "", None))
  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None))
  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long) =
    reporter(TestFailed(tracker.nextOrdinal(), message, "", "", None, None, "", "", None))
  def exampleSkipped(name: String, message: String, duration: Long) =
    reporter(TestPending(tracker.nextOrdinal(), message, "", None, None, "", "", None))
  def examplePending(name: String, message: String, duration: Long) =
    reporter(TestPending(tracker.nextOrdinal(), message, "", None, None, "", "", None))
}
