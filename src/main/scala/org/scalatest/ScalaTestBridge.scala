package org.scalatest

object ScalaTestBridge {
  def getIndentedText(testText: String, level: Int, includeIcon: Boolean) =
    Suite.getIndentedText(testText, level, includeIcon)
}
