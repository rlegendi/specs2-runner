package org.scalatest.specs2

import org.scalatest.finders.AstNode
import org.scalatest.finders.Selection
import org.scalatest.specs.Spec1Finder

/**
 * Finder class that can find test cases in the format ("should", "can") { "in" { ... } }
 *
 * @author rlegendi
 */
class Specs2Finder extends Spec1Finder {

  // TODO Specifications with function 'is'? (scope..)
  // TODO JUnitRunner in Specs2 searches for test extensively and it is well-tested
  //      Can't we use that aproach instead of playing with AstNodes?
  // TODO Test if it can find 2 tests in the same file (TwoSpecInSameFile.scala)

  override def find(node: AstNode): Option[Selection] = {
    super.find(node)
  }
}
