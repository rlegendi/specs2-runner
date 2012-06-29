package org.scalatest

import org.scalatest.events.IndentedText

/**
 * Simple utility class to get required but inaccessible features of the ScalaTest API.
 *
 * @author rlegendi
 */
object ScalaTestBridge {
  def getIndentedText(testText: String, level: Int, includeIcon: Boolean): IndentedText =
    Suite.getIndentedText(testText, level, includeIcon)
}
