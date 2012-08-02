package org.scalatest.specs2

import org.specs2.runner.SpecificationsFinder._
import org.scalatest.WrapWith
import org.specs2.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
@WrapWith(classOf[Specs2Runner])
class Index extends Specification {
  def is =

    examplesLinks("Example specifications")

  // See the SpecificationsFinder trait for the parameters of the 'specifications' method
  def examplesLinks(t: String) = specifications().foldLeft(t.title) { (res, cur) => res ^ see(cur) }
}
