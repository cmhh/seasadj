package org.cmhh.seasadj

import scala.util.{Failure, Success, Try}
import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Path

/**
 * Class to hold a single, complete specification.  That is, the configuration for a single time series.
 *
 * @param name name used to identify the specification
 * @param specs a map of parameter and value pairs
 */
case class Specification(name: String, specs: Specs) extends JSONOutput {
  import StringUtils._

  private def validate: Boolean = Validator.validate(this)
  require(validate, "Specification contains errors.")

  /**
   * Returns whether or not the specification contains a particular spec.
   *
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def containsSpec(name: String): Boolean = specs.contains(name)

  /**
   * Return single spec.
   *
   * @param name  name of spec, e.g. x11, series, automdl, etc.
   */
  def getSpec(name: String): Option[Spec] = specs.get(name)

  /**
   * Add empty spec and return as a new specfication.
   *
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def setSpec(name: String): Specification = {
    if (specs.contains(name)) this
    else copy(specs = specs + (name -> Map[String, SpecValue]()))
  }

  /**
   * Set spec and return as a new specification.
   * 
   * @param name name of spec, e.g. x11, series, automdl, etc.
   * @param spec individual spec, type [[Spec]], i.e. [[Map[String, SpecValue]]] 
   */
  def setSpec(name: String, spec: Spec): Specification = 
    copy(specs = specs + (name -> spec))

  /**
   * Remove spec and return as a new specification.
   *
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def removeSpec(name: String): Specification =
    if (specs.contains(name)) Specification(name, specs - name)
    else this

  /**
   * Returns whether or not the specification contains a specific spec parameter.
   *
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def containsParameter(parameter: String, name: String): Boolean = 
    if (!specs.contains(name)) false
    else specs(name).contains(parameter)

  /**
   * Return parameter value.
   *
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def getParameter(parameter: String, name: String): Option[SpecValue] = specs.get(name).flatMap(_.get(parameter))

  /**
   * Return all paramters with matching name.
   *
   * For example, data is a valid parameter in multiple specs.
   *
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   */
  def getAllParameter(parameter: String): Iterable[(String, String, SpecValue)] = 
    specs.keys.flatMap(key => specs(key).get(parameter) match {
      case None => None
      case Some(x) => Some((key, parameter, x))
    })

  /**
   * Set parameter value.
   * 
   * @param value [[SpecValue]], e.g. SpecNumArray(1,2,3)
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   * @param name name of spec, e.g. x11, series, automdl, etc. 
   */
  def setParameter(value: SpecValue, parameter: String, name: String): Specification = {
    val spec: Spec = specs.getOrElse(name, Map[String, SpecValue]()) + (parameter -> value)
    val newSpecs: Specs = specs + (name -> spec)
    copy(specs = newSpecs)
  }

  /**
   * Set parameter value.
   *
   * Set parameter value via [[String]] input&ndash;strings can be converted to
   * an appropriate [[SpecValue]] via [[Validator.specValue]].
   * 
   * @param value [[String]], e.g. `"(1,2,3)"`, `"([2 1] 1 0)(1 1 1)12"`, etc.
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def setParameter(value: String, parameter: String, name: String): Specification = {
    val newVal = Validator.specValue(name, parameter, value)
    newVal match {
      case Failure(e) => this
      case Success(v) => {    
        val spec: Spec = specs.getOrElse(name, Map[String, SpecValue]()) + (parameter -> v)
        val newSpecs: Specs = specs + (name -> spec)
        copy(specs = newSpecs)
      }
    }
  }

  /**
   * Remove parameter.
   *
   * @param parameter name of parameter, e.g. data, span, sigmalim, etc.
   * @param name name of spec, e.g. x11, series, automdl, etc.
   */
  def removeParameter(parameter: String, name: String): Specification = {
    if (!specs.contains(name)) this
    else if (!specs(name).contains(parameter)) this
    else Specification(this.name, specs + (name -> (specs(name) - parameter)))
  }

  /**
   * Whether the specification contains a composite spec.
   */
  def isComposite: Boolean = containsSpec("composite")

  /**
   * Whether the specfication contains a file parameter.
   *
   * 'file' is a valid parameter when using X11-ARIMA-SEATS via CLI.  However,
   * the implementation here generally assumes data is provided via 'data',
   * not 'file' and 'format'.  Importantly, this must be the case when calling
   * [[Adjustor.adjust]].
   */
  def containsFileParameter: Boolean = !getAllParameter("file").isEmpty

  /**
   * String representation of specification.
   *
   * The output will be in a format appropriate to save to the X11-ARIMA-SEATS
   * spc format.  The result could be saved to file, and adjusted externally.
   */
  override def toString: String = {
    def toString_(keys: IndexedSeq[String], accum: String): String = {
      if (keys.size == 0) accum
      else {
        val (h, t) =
          if (keys.contains("series")) ("series", keys.filter(_ != "series"))
          else if (keys.contains("composite")) ("composite", keys.filter(_ != "composite"))
          else (keys.head, keys.tail)
        val params = specs(h)
        if (params.size == 0)
          toString_(t, accum + (if (accum == "") "" else "\n") + h.toLowerCase + " {}")
        else
          toString_(
            t,
            accum + (if (accum == "") "" else "\n") + h.toLowerCase + " {\n  " +
              params.keys.toVector.map(param => {
                s"$param=${indent(params(param).toString, param.size + 3)}"
              }).mkString("\n  ") + "\n}"
          )
      }
    }
    toString_(specs.keys.toVector, "")
  }

  /**
   * Output spc file.
   *
   * @param file [[java.io.File]]
   */
  def toFile(file: File): Unit = {
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(toString)
    bw.close()
  }

  /**
   * Output spc file.
   *
   * @param folder name of folder to output spc file
   * @param name: name of spc file&ndash;defaults to specification name with spc extension appended
   */
  def toFile(folder: String, name: Option[String] = None): Unit = {
    val file: File = name match {
      case None => new File(s"$folder/${this.name}.spc")
      case Some(f) => new File(s"$folder/$f")
    }
    toFile(file)
  }

  /**
   * Output spc file.
   *
   * @param folder path to output spc file
   * @param name: name of spc file
   */
  def toFile(folder: Path, name: Option[String]): Unit = 
    toFile(folder.toFile.getAbsolutePath, name)

  /**
   * Output to disk.
   *
   * For internal use.  Creates spc and mta file, but also ensures relevant
   * working folders exist.
   *
   * @param 
   */
  def toDisk(folder: Path): Unit = {
    val f = folder.toFile
    val pathExists = {
      if (f.exists & f.isFile) false
      else if (f.exists & f.isDirectory) true
      else (f.mkdirs)
    }

    val oPathExists = {
      val p = new File(s"${f.getAbsolutePath}/o")
      if (p.exists & p.isFile) false
      else if (p.exists & p.isDirectory) true
      else (p.mkdirs)
    }

    val gPathExists = {
      val p = new File(s"${f.getAbsolutePath}/g")
      if (p.exists & p.isFile) false
      else if (p.exists & p.isDirectory) true
      else (p.mkdirs)
    }

    if (!pathExists) sys.error("Path does not exist, and could not be created.")
    if (!oPathExists) sys.error("Could not create output folder.")
    if (!gPathExists) sys.error("Could not create graphical output folder.")

    val sw = new BufferedWriter(new FileWriter(s"${f.getAbsolutePath}/$name.spc"))
    sw.write(toString)
    sw.close()

    val mw  = new BufferedWriter(new FileWriter(s"${f.getAbsolutePath}/$name.mta"))
    mw.write(s""""${f.getAbsolutePath}/$name" "${f.getAbsolutePath}/o/$name"""")
    mw.close()
  }

  /**
   * JSON representation of specification.
   *
   * @param nested if `true`, the result will be enclosed in curly braces
   */
  def toJSON(nested: Boolean): String = {
    def toJSON_(keys: IndexedSeq[String], accum: IndexedSeq[String]): IndexedSeq[String] = {
      if (keys.size == 0) accum
      else {
        val (h, t) =
          if (keys.contains("series")) ("series", keys.filter(_ != "series"))
          else if (keys.contains("composite")) ("composite", keys.filter(_ != "composite"))
          else (keys.head, keys.tail)
        val params = specs(h)
        if (params.size == 0)
          toJSON_(t, accum :+ s""""$h":null""")
        else {
          val keyvals = params.keys.toVector.map(param => {
            s""""$param":${params(param).toJSON}"""
          }).mkString(",")
          toJSON_(t, accum :+ s""""$h":{$keyvals}""")
        }
      }
    }
    val res = s""""$name":{""" + toJSON_(specs.keys.toVector, Vector[String]()).mkString(",") + "}"
    if (nested) res else s"{$res}"
  }

  /**
   * JSON representation of specification.
   *
   * JSON represenation of specification which is not nested.  This is a
   * convenience so that mulitiple specifications can be placed in a single
   * array.
   */
  def toJSON: String = toJSON(false)

  /**
   * Return new [[Specification]] with no external file dependencies.
   *
   * References to files in the input specification are matched up with a list
   * of files, and the content of each file is placed directly in the
   * specification, and the reference to the file removed.
   
   * @param root a folder to recursively search for candidate files 
   */
  def resolveFiles(root: File): Try[Specification] = {
    val files = FileUtils.listFiles(root, List("^.+(/o){1}$"))
    def resolve(x: List[(String, String, SpecValue)], accum: Specification): Specification = x match {
      case Nil => accum
      case h::t => {
        val matchedFile = FileUtils.matchFile(h._3.toString, files, true) match {
          case Some(f: File) => f
          case _ => sys.error(s"Could not find match for ${h._3}.")
        }
        val data = TimeSeries.fromFile(matchedFile, 0).toSpecValue
        val res1 = accum
          .setParameter(data._1, "data", h._1)
          .removeParameter("file", h._1)
        val res2 = 
          if (h._1 == "series" & !res1.containsParameter("start", h._1)) 
            res1.setParameter(data._2, "start", h._1)
          else res1
        val res3 = 
          if (res2.containsParameter("format", h._1)) res2.removeParameter("format", h._1)
          else res2
        resolve(t, res3)
      }
    }

    Try {
      resolve(this.getAllParameter("file").toList, this)
    }
  }

  /**
   * Return new [[Specification]] with no external file dependencies.
   *
   * References to files in the input specification are matched up with a list
   * of files, and the content of each file is placed directly in the
   * specification, and the reference to the file removed.
   
   * @param root a folder to recursively search for candidate files 
   */
  def resolveFiles(root: String): Try[Specification] = resolveFiles(new File(root))

  /**
   * Return new [[Specification]] with no external file dependencies.
   *
   * References to files in the input specification are matched up with a list
   * of files, and the content of each file is placed directly in the
   * specification, and the reference to the file removed.
   
   * @param files a collection of candidate files 
   */
  def resolveFiles(files: IndexedSeq[File]): Try[Specification] = {
    def resolve(x: List[(String, String, SpecValue)], accum: Specification): Specification = x match {
      case Nil => accum
      case h::t => {
        val matchedFile = FileUtils.matchFile(h._3.toString, files, true) match {
          case Some(f: File) => f
          case _ => sys.error(s"Could not find match for ${h._3}.")
        }
        val data = TimeSeries.fromFile(matchedFile, 0).toSpecValue
        val res1 = accum
          .setParameter(data._1, "data", h._1)
          .removeParameter("file", h._1)
        val res2 = 
          if (h._1 == "series" & !res1.containsParameter("start", h._1)) 
            res1.setParameter(data._2, "start", h._1)
          else res1
        val res3 = 
          if (res2.containsParameter("format", h._1)) res2.removeParameter("format", h._1)
          else res2
        resolve(t, res3)
      }
    }

    Try {
      resolve(this.getAllParameter("file").toList, this)
    }
  }
}

/**
 * Factory methods for [[Specification]].
 */
case object Specification {
  import FileUtils._

  /**
   * Create empty [[Specification]].
   */
  def apply(name: String): Specification = 
    Specification(name, Map[String, Spec]())

  def fromString(name: String, body: String): Try[Specification] = {
    @scala.annotation.tailrec
    def parse(s: List[Char], numparentheses: Int,
              numsquote: Int, numdquote: Int, level: Int,
              k1: String, k2: String, buffer: String,
              accum: Specs): Specs = s match {
      case Nil => accum
      case h::t => {        
        if ((numsquote % 2 == 0) & (numdquote % 2 == 0) & (h == '#')) {
          val pos = List('\n', '\r').map(t.indexOf(_)).filter(_ > -1)
          if (pos.size == 0) accum
          else parse(t.drop(pos.max), numparentheses, numsquote, numdquote, level, k1, k2, buffer, accum)
        }
        else if (h == '{'& (numsquote % 2 == 0) & (numdquote % 2 == 0)) 
          parse(t, numparentheses, numsquote, numdquote, level + 1, buffer.trim.toLowerCase, k2, "", accum)
        else if (h == '}' & (numsquote % 2 == 0) & (numdquote % 2 == 0)) {
          if (k2.trim != "" & buffer.trim != ""){
            val spec: Try[SpecValue] = Validator.specValue(k1, k2, buffer.trim)
            spec match {
              case Success(s) => {
                val m =
                  if (accum.contains(k1)) accum + (k1 -> (accum(k1) + (k2 -> s)))
                  else accum + (k1 -> Map[String, SpecValue](k2 -> s))
                parse(t, numparentheses, numsquote, numdquote, level - 1, k1, "", "", m)
              }
              case Failure(e) =>
                throw new IllegalArgumentException(s"Couldn't create SpecValue from:\n($k1, $k2, ${buffer.trim})")
            }
          }
          else {
            if (accum.contains(k1))
              parse(t, numparentheses, numsquote, numdquote, level - 1, "", "", "", accum)
            else
              parse(
                t, numparentheses, numsquote, numdquote, level - 1,
                "", "", "", accum + (k1 -> Map[String, SpecValue]())
              )
          }
        }
        else if ((h == '(' | h == ')') & numsquote % 2 == 0 & numdquote % 2 == 0)
          parse(t, numparentheses + 1, numsquote, numdquote, level, k1, k2, buffer + h, accum)
        else if ((h == '\n' | h == '\r' | h == ' ') & level == 0) 
          parse(t, numparentheses, numsquote, numdquote, level, k1, k2, buffer, accum)
        else if ((h == '\n' | h == '\r') & (numparentheses % 2 != 0 | numsquote % 2 != 0 | numdquote % 2 != 0)) 
          parse(t, numparentheses, numsquote, numdquote, level, k1, k2, buffer + " ", accum)
        else if (h == '\n' | h == '\r') {
          if (buffer.trim != "") {
            val spec: Try[SpecValue] = Validator.specValue(k1, k2, buffer.trim)
            spec match {
              case Success(s) => {
                val m =
                  if (accum.contains(k1)) accum + (k1 -> (accum(k1) + (k2 -> s)))
                  else accum + (k1 -> Map[String, SpecValue](k2 -> s))
                parse(t, numparentheses, numsquote, numdquote, level, k1, "", "", m)
              }
              case Failure(e) => 
                throw new IllegalArgumentException(s"Couldn't create SpecValue from:\n($k1, $k2, ${buffer.trim})\n")
            }
          }
          else 
            parse(t, numparentheses, numsquote, numdquote, if (h == '}') level - 1 else level, k1, k2, buffer, accum)
        }
        else if (h == '=') 
          parse(t, numparentheses, numsquote, numdquote, level, k1, buffer.trim.toLowerCase, "", accum)
        else if (h == '\'' | h == '"') {
          val squote = if (h == '\'') numsquote + 1 else numsquote
          val dquote = if (h == '"') numdquote + 1 else numdquote
          parse(t, numparentheses, squote, dquote, level, k1, k2, buffer, accum)
        }
        else 
          parse(t, numparentheses, numsquote, numdquote, level, k1, k2, buffer + h, accum)
      }
    }

    Try({
      val specs = parse(
        body.toList, 0, 0, 0, 0, "", "", "",
        Map[String, Map[String, SpecValue]]()
      )

      Specification(name, specs)
    })
  }

  def fromFile(name: String, file: String): Try[Specification] = {
    val body = sourceFromPath(file).getLines().mkString("\n")
    fromString(name, body)
  }

  def fromFile(name: String, file: File): Try[Specification] = fromFile(name, file.toString)
  def fromFile(name: String, file: Path): Try[Specification] = fromFile(name, file.toString)

  /**
   * Create a basic [[Specification]] from a [[TimeSeries]]&ndash;x11 variant.
   *
   * @param name name
   * @param data time series
   */
  def X11(name: String, data: TimeSeries): Specification = {
    val (seriesData, seriesStart) = data.toSpecValue
    val seriesSpec: Spec = Map(
      "title" -> SpecString(name), 
      "data" -> seriesData, 
      "start" -> seriesStart
    )
    val x11Spec: Spec = Map(
      "sigmalim" -> SpecNumArray(Some(1.8), Some(2.8))
    )
    Specification(
      name,
      Map("series" -> seriesSpec, "x11" -> x11Spec)
    )
  }

  /**
   * Create a basic [[Specification]] from a [[TimeSeries]]&ndash;SEATS variant.
   *
   * @param name name
   * @param data time series
   */
  def Seats(name: String, data: TimeSeries): Specification = {
    val (seriesData, seriesStart) = data.toSpecValue
    val seriesSpec: Spec = Map(
      "title" -> SpecString(name), 
      "data" -> seriesData, 
      "start" -> seriesStart
    )
    Specification(
      name,
      Map("series" -> seriesSpec, "seats" -> Map[String,SpecValue]())
    )
  }
}