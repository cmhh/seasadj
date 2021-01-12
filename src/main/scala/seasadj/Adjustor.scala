package org.cmhh.seasadj

import scala.util.{Failure, Success, Try}
import java.io.{ BufferedReader, File, InputStreamReader }
import java.nio.file.Path

/**
 * Singleton object used to seasonally adjust [[Specification]] and [[Specificatons]] objects.
 */
object Adjustor {
  import FileUtils._

  /**
   * Perform seasonal adjustment.
   *
   * @param specification [[Specification]]
   * @param save list of time series outputs to keep
   */
  def adjust(specification: Specification, save: Seq[String]): Try[Adjustment] = {
    Try({
      val workingDir: Path = getTempFolder
      specification.toDisk(workingDir)
      val cmd = Array("x13ashtml", "-m", s"$workingDir/${specification.name}", "-g", s"$workingDir/g")
      val (stdout, stderr) = runProcess(cmd)
      val results = importGmt(s"$workingDir/g/${specification.name}.gmt", save)
      val summary = importUdg(s"$workingDir/g/${specification.name}")
      deleteFolder(workingDir)
      Adjustment(specification.name, results, summary)
    })
  }

  /**
   * Perform seasonal adjustment.
   *
   * @param specification [[Specification]]
   * @param save list of time series outputs to keep
   */
  def adjust(specification: Specification): Try[Adjustment] = adjust(specification, Vector[String]())

  /**
   * Perform seasonal adjustment.
   * 
   * Seasonal adjustment can produce a large number of time series components, 
   * and the ability to filter is included here.  It might be better to 
   * implement this elsewhere, but each series kept must be read from disk, 
   * so controlling what is read here can improve response times. 
   *
   * @param specifications [[Specification]]
   * @param save list of time series outputs to keep
   */
  def adjust(specifications: Specifications, save: Seq[String]): Try[Adjustments] = {
    Try({
      val workingDir = getTempFolder
      val mta = specifications.toDisk(workingDir)
      val cmd = Array("x13ashtml", "-m", s"${mta.dropRight(4)}", "-g", s"$workingDir/g")
      val (stdout, stderr) = runProcess(cmd)
      val results: IndexedSeq[Adjustment] = specifications
        .map(_.name)
        .map(name => {
          Adjustment(
            name,
            importGmt(s"$workingDir/g/$name.gmt", save),
            importUdg(s"$workingDir/g/$name")
          )
        })
      deleteFolder(workingDir)
      Adjustments(results)
    })
  }

  /**
   * Perform seasonal adjustment.
   *
   * @param specifications [[Specification]]
   */
  def adjust(specifications: Specifications): Try[Adjustments] = adjust(specifications, Vector[String]())

  /**
   * Run a system process.
   *
   * @param cmd An array, e.g. `Array("x13ashtml", "-m", "foo", "-g", "bar/g")` or `Array("ls", "-la")`.
   */
  private def runProcess(cmd: Array[String]): (String, String) = {
    val p = Runtime.getRuntime.exec(cmd)
    val stdInput = new BufferedReader(new InputStreamReader(p.getInputStream))
    val stdError = new BufferedReader(new InputStreamReader(p.getErrorStream))
    val stdout = stdInput.lines.toArray.map(_.asInstanceOf[String]).mkString("\n")
    val stderr = stdError.lines.toArray.map(_.asInstanceOf[String]).mkString("\n")
    p.waitFor
    p.destroy
    stdInput.close
    stdError.close

    (stdout, stderr)
  }

  /**
   * Import series listed in graphical metadata (gmt) file.
   *
   * @param file path to gmt file as [[String]]
   * @param save list of time series outputs to keep
   */
  private def importGmt(file: String, save: Seq[String]): Map[String, TimeSeries] = {
    val records = sourceFromPath(file)
      .getLines()
      .toList
      .map(rec => {
        val pos = rec.indexOf(" ")
        (rec.take(pos).trim, rec.drop(pos).trim)
      })
      .filter(rec => {
        if (save.size == 0) true
        else save.map(x => x.toLowerCase).contains(rec._1.toLowerCase)
      })

    def importGmt_(records: List[(String, String)], accum: Map[String, TimeSeries]): 
      Map[String, TimeSeries] = records match {
      case Nil => accum
      case h::t => {
        val (name, path) = h
        val series: Try[TimeSeries] = Try {
          TimeSeries.fromFile(path, 2)
        }
        series match {
          case Success(x) => importGmt_(t, accum + (name -> x))
          case Failure(x) => importGmt_(t, accum)
        }
      }
    }
    importGmt_(records, Map[String, TimeSeries]())
  }

  /**
   * Import diagnostics from udg / xdg file.
   *
   * @param file path to udg / xdg file without extension
   */
  private def importUdg(file: String): Map[String, DiagnosticValue] = {
    val extension =
      if ((new File(s"$file.udg")).exists) "udg"
      else if ((new File(s"$file.xdg")).exists) "xdg"
      else sys.error("xdg / udg not found.")
    val stats = sourceFromPath(s"$file.$extension").getLines().toList
    def importUdg_(stats: List[String], accum: Map[String, DiagnosticValue]): 
      Map[String, DiagnosticValue] = stats match {
      case Nil => accum
      case h::t => {
        val stat = h.split(':').map(_.trim)
        importUdg_(t, accum + (stat(0) -> DiagnosticValue.fromString(stat(1))))
      }
    }
    importUdg_(stats, Map[String, DiagnosticValue]())
  }
}