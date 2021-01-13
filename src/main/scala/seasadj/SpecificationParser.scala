package org.cmhh.seasadj

import org.cmhh.seasadj._
import scala.util.{Try, Success, Failure}

/**
 * Static object used to parse SPC files.
 */
object SpecificationParser {
  // linebreak
  val linebreak = "(?:(?:\r\n)|\n)"
  // whitespace
  val ws = s"[ \r\n]*"
  // left brace with surrounding whitespace / linebreaks
  val lbrace = s" *${linebreak}? *\\{ *${linebreak}? *"
  // right brace with surrounding whitespace / linebreaks
  val rbrace = s" *${linebreak}? *\\} *${linebreak}? *"
  // anything that could be a specificaton or parameter name
  val name = "(?:[a-zA-Z]+[a-zA-Z0-9]*)"
  // anything permitted inside a spec value
  val anytext = "(?:[a-zA-Z]+[a-zA-Z0-9\r\n\\(\\)\"\'\\.,=\\\\/ -]*)"
  // anything permitted on the rhs of a spec parameter
  val anyrhs = "(?:[a-zA-Z\\(]+[a-zA-Z0-9\r\n\\(\\)\"\'\\.,\\\\/ -]*)"
  // high-level match for entire spec, i.e. spec { specname}
  val specification = s"${name}${lbrace}(?:(?:$ws)|(?:${anytext}))${rbrace}"
  // high-level match for entire spec 'file', i.e. name { param = value}\nname { param = value}...
  val specifications = s"^( *${linebreak}? *${specification} *${linebreak}? *)+$$"

  /**
    * Make a single regex which matches 'any of' several others.
    *
    * @param rs list of regular expression strings
    * @return A regular expression string
    */
  def anyOf(rs: Seq[String]): String = 
    "(" + rs.map(r => s"(?:$r)").mkString("|") + ")"

  /**
    * Remove comments from a specification.
    * 
    * Comments are denoted by #.  Remove any lines that start with #, or remove
    * content after # if it appears part-way through a line.
    *
    * @param str the body of a SPC file
    * @return the body of a SPC file with comments removed.
    */
  def stripComment(str: String): String = {
    if (str.startsWith("#")) ""
    else if (str.contains("#")) str.take(str.indexOf("#"))
    else str
  }

  /**
    * Standardise newlines, remove comments, and trim some whitespace from the
    * content of a SPC file.
    *
    * @param str the body of a SPC file
    * @return tidier version of the body of a SPC file
    */
  def preprocess(str: String): String = 
    str
      .split("\\R")
      .map(stripComment) 
      .map(_.trim)
      .filter(line => line != "")
      .mkString("\n")

  /**
    * Split body of SPC files into individual specs.
    * 
    * Extract instances of the pattern `spec { stuff }`, where spec is, for example, {{series}}, {{x11}}, etc.
    *
    * @param str
    * @return
    */
  def parseSpecs(str: String): Try[Seq[String]] = Try { 
    val processed = preprocess(str)
    if (!processed.matches(specifications)) 
      throw new IllegalArgumentException("Specification is not formatted correctly.")
    specification.r.findAllIn(processed).toList
  }

  /**
    * Split the body of specifications into name-value pairs.
    * 
    * Given text with pattern `spec { stuff }`, convert to {{(spec, stuff)}}.
    *
    * @param str specification text
    * @return key-value pair
    */
  def parseSpecNameAndBody(str: String): Try[(String, String)] = {
    try {
      val r = s"^($name)${lbrace}((?:$ws)|(?:$anytext))${rbrace}$$".r
      val r(specname, specbody) = str
      Success((specname, specbody))
    } catch {
      case _: Throwable => Failure(
        new IllegalArgumentException(s"Failed to extract name and body from ${str}")
      )
    }
  }
  
  /**
    * Split body of spec into name-value pairs.
    * 
    * Given `stuff` extracted from `spec { stuff }`, convert `stuff` to 
    * {{(key, value)}}.  For example, {{("sigamlim", "(1.8, 2.8)")}}
    *
    * @param str input text, e.g. "sigmalim=(1.8,2.8)"
    * @return [[Try[Map[String, String]]]]
    */
  def parseArgs(str: String): Try[Map[String, String]] = Try {
    // when '(' is detected, find the remaining sequence up to ')'
    def complete(s: String, numLeft: Int, numRight: Int, accum: String): (String, String) = {
      if (s == "") throw new IllegalArgumentException(s"""Invalid specification value: "${str}".""")
      else if (s.take(1) == "(") complete(s.drop(1), numLeft + 1, numRight, accum + s.take(1))
      else if (s.take(1) == ")") {
        if (numLeft == (numRight + 1)) (s.drop(1).trim, accum + ")")
        else complete(s.drop(1), numLeft, numRight + 1, accum + ")")
      }
      else complete(s.drop(1), numLeft, numRight, accum + s.take(1))
    }

    def parse(
      s: String, accum: Seq[(String, String)]
    ): Seq[(String, String)] = {
      if (s == "") accum
      else if (s.contains("=")) {
        val i = s.indexOf("=")
        val left = s.take(i).trim
        val right = s.drop(i + 1).trim
        if (right.take(1) == "(") {
          val (rest, value) = complete(right, 0, 0, "")
          parse(rest, accum :+ (left, StringUtils.unquote(value)))
        } else {
          val j = right.indexOf("\n")
          if (j == -1) accum :+ (left, right)
          else parse(right.drop(j), accum :+ (left, StringUtils.unquote(right.take(j))))
        }
      } else throw new IllegalArgumentException(s"""Could not parse parameters from "$s"""")
    }

    parse(str, Seq.empty).toMap
  }

  /**
    * Convert body of a SPC file into a [[Specs]] object.
    *
    * @param str
    * @return
    */
  def parse(str: String): Try[Map[String, Map[String, SpecValue]]] = Try {
    val res = parseSpecs(str) match {
      case Success(specs) => 
        specs.map(parseSpecNameAndBody).map(_ match {
          case Success(spec) => 
            parseArgs(spec._2) match {
              case Success(params) => 
                (
                  spec._1 ->
                  params.map(param => Validator.specValue(spec._1, param._1, param._2) match {
                    case Success(s) => (param._1 -> s)
                    case Failure(e) => throw e
                  })
                )
              case Failure(e) => throw e
            }
          case Failure(e) => throw e
        })
      case Failure(e) => throw e
    }
    res.toMap
  }
}