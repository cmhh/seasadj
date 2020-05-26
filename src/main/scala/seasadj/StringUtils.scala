package org.cmhh.seasadj

/**
 * Miscellaneous methods for working with strings.
 */
object StringUtils {
  def indent(s: String, size: Int): String =
    s.replace("\n", "\n" + (1 to size).map(x => " ").mkString)
}