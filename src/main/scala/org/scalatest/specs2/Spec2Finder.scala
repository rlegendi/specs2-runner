package org.scalatest.specs2

import org.scalatest.finders.AstNode
import org.scalatest.finders.Selection
import org.scalatest.specs.Spec1Finder

class Spec2Finder extends Spec1Finder {

  // TODO Specifications with function 'is'? (scope..)

  override def find(node: AstNode): Option[Selection] = {
    super.find(node)
  }
}
