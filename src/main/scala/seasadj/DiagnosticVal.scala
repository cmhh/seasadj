package org.cmhh.seasadj

/**
 * Specification value.
 */
abstract sealed class DiagnosticValue {
  def toString: String
  def toJSON: String 
}

/**
 * String diagnostic.
 */
case class DiagnosticString(value: String) extends DiagnosticValue {
  override def toString = value
  override def toJSON = s""""$value""""
}

/**
 * String diagnostic.
 */
case class DiagnosticStringArray(value: String*) extends DiagnosticValue {
  override def toString = value.mkString(" ")
  override def toJSON = s"""[${value.map(x => s""""$x"""").mkString(",")}]"""
}

/**
 * Integer diagnostic.
 */
case class DiagnosticInt(value: Int) extends DiagnosticValue {
  override def toString = value.toString
  override def toJSON = value.toString
}

/**
 * Integer diagnostic.
 */
case class DiagnosticIntArray(value: Int*) extends DiagnosticValue {
  override def toString = value.mkString(" ")
  override def toJSON = s"""[${value.mkString(",")}]"""
}

/**
 * Numeric diagnostic.
 */
case class DiagnosticNum(value: Double) extends DiagnosticValue {
  override def toString = value.toString
  override def toJSON = value.toString
}

/**
 * Numeric diagnostic.
 */
case class DiagnosticNumArray(value: Double*) extends DiagnosticValue {
  override def toString = value.mkString(" ")
  override def toJSON = s"""[${value.mkString(",")}]"""
}

/**
 * Factory methods.
 */
case object DiagnosticValue {
  def fromString(str: String): DiagnosticValue = {
    val tokens = tokenize(str)
    if (allInt(str)) 
      if (tokens.size == 1)
        DiagnosticInt(tokens(0).toInt)
      else 
        DiagnosticIntArray(tokens.toList.map(_.toInt): _*)
    else if (allDouble(str))
      if (tokens.size == 1)
        DiagnosticNum(tokens(0).toDouble)
      else
        DiagnosticNumArray(tokens.toList.map(_.toDouble): _*)
    else
      DiagnosticString(str)
  }

  private def tokenize(s: String): Array[String] = 
    s.trim.replaceAll("\t", " ").replaceAll(" +", " ").split(' ')
  
  private def isInt(s: String): Boolean = s.matches("^\\d+$")
  private def isDouble(s: String): Boolean = s.matches("^(\\+|\\-)*\\d+(\\.)*\\d*(E(\\+|\\-)*\\d+)*$")

  private def allInt(s: String): Boolean = {
    val ints = tokenize(s).map(isInt(_))
    ints.foldLeft(true)(_ & _)
  }

  private def allDouble(s: String): Boolean = {
    val ints = tokenize(s).map(isDouble(_))
    ints.foldLeft(true)(_ & _)
  }
}