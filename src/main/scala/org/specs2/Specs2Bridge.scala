package org.specs2

import org.specs2.reflect.Classes
import org.specs2.specification.Fragments
import org.specs2.specification.SpecificationStructure
import org.specs2.main.Arguments
import org.specs2.specification.ExecutingSpecification
import org.specs2.specification.ExecutingFragment
import org.specs2.specification.SpecName
import org.specs2.execute.Pending
import org.specs2.specification.ExecutedResult
import org.specs2.execute.DecoratedResult
import org.specs2.execute.Skipped
import org.scalatest.Reporter
import org.specs2.execute.Failure
import org.specs2.execute.Success
import org.scalatest.Tracker
import org.scalatest.events.TestStarting
//import reporter.Reporter
import scala.reflect.NameTransformer
import org.scalatest.events.MotionToSuppress
import org.specs2.specification.ExecutedSpecStart
import org.specs2.specification.ExecutedSpecEnd
import org.scalatest.events.TestSucceeded
import org.scalatest.Suite
import org.scalatest.ScalaTestBridge
import org.scalatest.events.TestFailed
import org.specs2.execute.Error
import org.scalatest.events.TestPending
import org.scalatest.specs2.Utils
import org.specs2.specification.ExecutedFragment

object Specs2Bridge {
  def tryToCreateSpecification[T <: AnyRef](specs2Class: Class[_ <: SpecificationStructure]): SpecificationStructure = {
    // This is a private utility class that is inaccessible from outer packages
    Classes.tryToCreateObject(specs2Class.getName).get
  }

  // TODO Content is package-private, this is a workaround, consult with Eric
  // ERIC: that's intentional. It is to avoid the namespace of the Specification inheritor to be polluted with something he never uses
  // one way to make things nicer is to add "implicit" to your getContent definition. This way, any spec can be seen as the list
  // of Fragments it is holding
  implicit def getContentFor(spec: SpecificationStructure): Fragments = spec.content
}
