package org.scalatest.specs2.notifiers

import org.specs2.execute.Details
import org.specs2.reporter.Notifier

/**
 * This is a simple notifier just for testing.
 *
 * @author rlegendi
 */
class EmptyNotifier extends Notifier {
  def specStart(title: String, location: String): Unit = {
    println(">>> specStart: " + title + "@" + location)
  }

  def specEnd(title: String, location: String): Unit = {
    println("<<< specEnd: " + title + "@" + location)
  }

  def contextStart(text: String, location: String): Unit = {
    println(">>> contextStart: " + text + "@" + location)
  }

  def contextEnd(text: String, location: String): Unit = {
    println("<<< contextEnd: " + text + "@" + location)
  }

  def text(text: String, location: String): Unit = {
    println(">>> text: " + text + "@" + location)
  }

  def exampleStarted(name: String, location: String): Unit = {
    println(">>> exampleStarted: " + name + "@" + location)
  }

  def exampleSuccess(name: String, duration: Long): Unit = {
    println("<<< exampleSuccess: " + name + ", t=" + duration)
  }

  private def testFailed(name: String, message: String, location: String, f: Throwable, details: Option[Details], duration: Long): Unit = {
    println("<<< testFailed: " + name + ", " + message + ", " + location + ", " + f + ", " + details + ", " + duration)
  }

  def exampleFailure(name: String, message: String, location: String, f: Throwable, details: Details, duration: Long): Unit = {
    println("<<< exampleFailure: " + name + ", t=" + message)
  }

  def exampleError(name: String, message: String, location: String, f: Throwable, duration: Long): Unit = {
    println("<<< exampleError: " + name + ", t=" + message)
  }

  def exampleSkipped(name: String, message: String, duration: Long): Unit = {
    println("<<< exampleSkipped: " + name + ", t=" + message)
  }

  def examplePending(name: String, message: String, duration: Long): Unit = {
    println("<<< examplePending: " + name + ", t=" + message)
  }
}
