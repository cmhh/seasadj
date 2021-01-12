package org.cmhh.seasadj

import scala.util.{Failure, Success, Try}
import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Path

/**
 * Class to hold a several specifications.  That is, the configuration for a collection of time series.
 *
 * @param specifications a collection of [[Specification]]s
 */
case class Specifications(specifications: IndexedSeq[Specification]) extends JSONOutput {
  import FileUtils._

  private def validate: Boolean = Validator.validate(this)
  require(validate, "Specifications contain errors.")

  require(
    {specifications.map(_.name).distinct.size == specifications.size}, 
   "Duplicate names in specifications."
  )

  lazy val size = specifications.size

  /**
   * Combine [[Specifications]].
   *
   * @param that [[Specifications]]
   */
  def ++(that: Specifications): Specifications = 
    Specifications(specifications ++ that.specifications)

  /**
   * Add [[Specification]] to [[Specifications]].
   *
   * @param that [[Specification]]
   */
  def :+(that: Specification): Specifications = 
    Specifications(specifications :+ that)

  /**
   * Subset [[Specifications]].
   *
   * @param f mapping from [[Specification]] to [[Boolean]]
   */
  def filter(f: Specification => Boolean): Specifications = 
    Specifications(specifications.filter(f))

  /**
   * Select [[Specification]] by name.
   *
   * @param name name of [[Specification]]
   */
  def apply(name: String): Option[Specification] = {
    val s = specifications.filter(_.name == name) 
    if (s.size == 0) None
    else Some(s(0))
  }

  /**
   * String representation.
   */
  override lazy val toString = 
    specifications.map(specification => {
      val adjustmentType = 
        if (specification.isComposite) "composite"
        else if (specification.containsSpec("x11")) "x11"
        else if (specification.containsSpec("seats")) "seats"
        else "unknown"
      s"${specification.name}\t${adjustmentType}"
    }).mkString("\n")

  /**
   * Apply function to each specification.
   *
   * @param f mapping from [[Sepecification]] to generic type `T`
   */
  def map[T](f: (Specification => T)): IndexedSeq[T] =
    specifications.map(f)

  /**
   * Apply function to each specification.
   *
   * @param f mapping from [[Sepecification]] to generic sequence of type `T`
   */
  def flatMap[T](f: Specification => IndexedSeq[T]): IndexedSeq[T] =
    specifications.flatMap(f)

  /**
   * Write each specification to file.
   *
   * @param folder folder where each specification will be written
   */
  def toFile(folder: String): Unit = specifications.foreach(_.toFile(folder))

  /**
   * Write each specification to file.
   *
   * @param folder folder where each specification will be written
   */
  def toFile(folder: Path): Unit = specifications.foreach(_.toFile(folder, None))

  /**
   * Output to disk.
   *
   * For internal use.  Creates a spc file for each specification, an mta file,
   * and also ensures relevant working, folders exist.
   *
   * @param folder target folder as [[java.io.File]]
   * @param name optional file name used as name of mta file
   */
  def toDisk(folder: File, name: Option[String] = None): String = {
    val pathExists = {
      if (folder.exists & folder.isFile) false
      else if (folder.exists & folder.isDirectory) true
      else (folder.mkdirs)
    }

    val oPathExists = {
      val p = new File(s"${folder.getAbsolutePath}/o")
      if (p.exists & p.isFile) false
      else if (p.exists & p.isDirectory) true
      else (p.mkdirs)
    }

    val gPathExists = {
      val p = new File(s"${folder.getAbsolutePath}/g")
      if (p.exists & p.isFile) false
      else if (p.exists & p.isDirectory) true
      else (p.mkdirs)
    }

    if (!pathExists) sys.error("Path does not exist, and could not be created.")
    if (!oPathExists) sys.error("Could not create output folder.")
    if (!gPathExists) sys.error("Could not create graphical output folder.")

    specifications.map(specification => {
      val sw = new BufferedWriter(new FileWriter(s"${folder.getAbsolutePath}/${specification.name}.spc"))
      sw.write(specification.toString)
      sw.close()
    })

    val mta = specifications
      .map(specification => s""""${folder.getAbsolutePath}/${specification.name}" "${folder.getAbsolutePath}/o/${specification.name}"""")
      .mkString("\n")

    val mtaPath = name match {
      case None => getTempFile(folder.toString, None, Some(".mta")).toString
      case Some(f) => if (f.takeRight(4) == ".mta") f else s"$f.mta"
    }

    val mw  = new BufferedWriter(new FileWriter(mtaPath))
    mw.write(mta)
    mw.close()

    mtaPath
  }

  /**
   * Output to disk.
   *
   * For internal use.  Creates a spc file for each specification, an mta file,
   * and also ensures relevant working, folders exist.
   *
   * @param folder target folder as [[String]]
   * @param name optional file name used as name of mta file
   */
  def toDisk(folder: String, name: Option[String]): String = toDisk(new File(folder), name)

  /**
   * Output to disk.
   *
   * For internal use.  Creates a spc file for each specification, an mta file,
   * and also ensures relevant working, folders exist.
   *
   * @param folder target folder as [[java.nio.file.Path]]
   * @param name optional file name used as name of mta file
   */
  def toDisk(folder: Path, name: Option[String]): String = toDisk(folder.toFile, name)

  /**
   * Output to disk.
   *
   * For internal use.  Creates a spc file for each specification, an mta file,
   * and also ensures relevant working, folders exist.
   *
   * @param folder target folder as [[String]]
   */
  def toDisk(folder: String): String = toDisk(new File(folder), None)

  /**
   * Output to disk.
   *
   * For internal use.  Creates a spc file for each specification, an mta file,
   * and also ensures relevant working, folders exist.
   *
   * @param folder target folder as [[java.nio.Path]]
   */
  def toDisk(folder: Path): String = toDisk(folder.toFile, None)

  /**
   * Convert to JSON string.
   */
  def toJSON: String =
    "{" + specifications.map(spec => spec.toJSON(true)).mkString(",") + "}"
}

/**
 * Factory methods for [[Specifications]].
 */
case object Specifications {
  import FileUtils._
  
  /**
   * Create [[Specifications]] from JSON string.
   *
   * @param body JSON string
   */
  def fromJSONString(body: String): Try[Specifications] = Try {
    import upickle.default._

    val parsed = ujson.read(body)

    val specs: IndexedSeq[Specification] = parsed match {
      case o: ujson.Obj => 
        val names: IndexedSeq[String] = o.obj.keys.toVector
        names.map(name => {
          val s: Map[String, Map[String, SpecValue]] = parsed(name) match {
            case o: ujson.Obj => 
              val specs: List[String] = o.obj.keys.toList.map(_.toString)
              specs.map(spec => {
                val params: Map[String, SpecValue] = parsed(name)(spec) match {
                  case o: ujson.Obj => 
                    val params: List[String] = o.obj.keys.toList.map(_.toString)
                    params.map(param => {
                      val specVal: SpecValue = Validator.specValue(spec, param, parsed(name)(spec)(param).toString, JSON) match {
                        case Success(specval) => specval
                        case Failure(e) => throw e
                      }
                      (param -> specVal)
                    }).toMap
                  case ujson.Null => Map.empty
                  case _ => throw new IllegalArgumentException(s"""Unexpected input for "$name" -> "$spec"""")
                }
                (spec -> params)
              }).toMap
            case _ => throw new IllegalArgumentException(s"""Unexpected input for series "$name."""")
          }
          Specification(name, s)
        })
      case _ => throw new IllegalArgumentException("Input must be an object.")
    }

    Specifications(specs)    
  }

  /**
   * Create [[Specifications]] from JSON file.
   *
   * @param file JSON file as [[String]]
   */
  def fromJSON(file: String): Try[Specifications] = Try {
    val body = sourceFromPath(file).getLines().mkString
    fromJSONString(body) match {
      case Success(x) => x
      case Failure(e) => throw e
    }
  }

  /**
   * Create [[Specifications]] from JSON file.
   *
   * @param file JSON file as [[java.io.File]]
   */
  def fromJSON(file: File): Try[Specifications] = fromJSON(file.toString)

  /**
   * Create [[Specifications]] from JSON file.
   *
   * @param file JSON file as [[Java.nio.file.Path]]
   */
  def fromJSON(file: Path): Try[Specifications] = fromJSON(file.toString)
}