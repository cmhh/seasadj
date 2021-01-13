package org.cmhh.seasadj

/**
 * Miscellaneous methods for working with strings.
 */
object StringUtils {
  def indent(s: String, size: Int): String =
    s.replace("\n", "\n" + (1 to size).map(x => " ").mkString)

  def unquote(s: String): String = 
    if ((s.take(1) == "\"" & s.takeRight(1) == "\"") | (s.take(1) == "'" & s.takeRight(1) == "'"))
      s.drop(1).dropRight(1)
    else 
      s
  
  def escape(s: String): String = 
    s.replace("\"", "\\\"").replace("\n", "\\n")
}