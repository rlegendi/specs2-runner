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
  private def getDecodedName(name:String): Option[String] = {
    val decoded = NameTransformer.decode(name)
    if(decoded == name) None else Some(decoded)
  }

  def notifyScalaTest(spec2: SpecificationStructure, tracker: Tracker, reporter: Reporter) = (executed: Seq[ExecutedFragment]) => {
    
    // TODO Probably I will need some nice descriptions like JUnitDescriptionsFragments.mapper() has
    
     executed foreach {
        case (res @ ExecutedResult(_, result, timer, _,_)) => {
          //notifier.fireTestStarted(desc)
          reporter(TestStarting(tracker.nextOrdinal(), spec2.getClass.getSimpleName, spec2.getClass.getName, Some(spec2.getClass.getName),
            getDecodedName(spec2.getClass.getSimpleName), res.text(), exampleName, getDecodedName(testName), Some(MotionToSuppress), None, Some(spec.getClass.getName)))

          
          result match {
            case f @ Failure(m, e, st, d)                     =>
              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            case e @ Error(m, st)                             =>
              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            case DecoratedResult(_, f @ Failure(m, e, st, d)) =>
              notifier.fireTestFailure(new notification.Failure(desc, junitFailure(f)))
            case DecoratedResult(_, e @ Error(m, st))         =>
              notifier.fireTestFailure(new notification.Failure(desc, args.traceFilter(e.exception)))
            case Pending(_) | Skipped(_, _)                   => notifier.fireTestIgnored(desc)
            case Success(_,_) | DecoratedResult(_, _)         => ()
          }
          notifier.fireTestFinished(desc)
        }
        case (desc, ExecutedSpecStart(_,_,_)) => notifier.fireTestRunStarted(desc)
        case (desc, ExecutedSpecEnd(_,_,_))   => notifier.fireTestRunFinished(new org.junit.runner.Result)
        case (desc, _)                        => // don't do anything otherwise too many tests will be counted
      }
  }

}
