package org.cmhh.seasadj.tools

import org.cmhh.seasadj.{Specification, Specifications}
import org.cmhh.seasadj.FileUtils._
import scala.util.{Failure, Success}
import java.io.{File}

// command line tool to convert SA setup in a folder to JSON appropriate to send to the service.
// have versions which assume data is in d/ and also which get data from Sybase.

/** 
 * Import a single folder.
 *
 * Will scan a folder for spc files and create a [[Specification]] for each.
 * If no mta files are found, a single JSON file will be output containing
 * all spc files found, otherwise, a single JSON file will be output for each
 * mta file found.
 */
object ImportInputs extends App {
  if (args.size < 2) {
    println("Usage: java -jar seasadj.jar <input folder> [output folder | output file]")
  } else { 
    val root: File = new File(args(0))
    if (!root.exists) sys.error("Input location does not exist.")
    if (!root.isDirectory) sys.error("Input location is not a folder.")

    val mta = root.listFiles.filter(f => f.getAbsolutePath.toLowerCase.matches("^.+(.mta)$"))
    val spc = root.listFiles.filter(f => f.getAbsolutePath.toLowerCase.matches("^.+(.spc)$"))
    val files = listFiles(root, List("^.+(/o){1}$"))

    if (mta.size == 0) {
      val outpath: File = 
        if (args.size == 2) new File(args(1)) 
        else getTempFile(".", None, Some(".json")).toFile
      if (!((new File(outpath.getParent)).mkdirs | (new File(outpath.getParent)).exists)) 
        sys.error(s"Folder does not exist: ${outpath.getParent}.")
      if (outpath.isDirectory) sys.error("Output location is a folder.")

      val specs: IndexedSeq[Specification] = spc.map(f => {
        val name = f.getName.split('.').head
        Specification.fromFile(name, f) match {
          case Success(s) => s.resolveFiles(files) match {
            case Success(t) => t
            case Failure(e) => sys.error(s"Could not resolve all files for ${f.getName}.")            
          }
          case Failure(e) => sys.error(s"Failed to import ${f.getName}.\n\n===\n${e.getMessage}\n===\n\n")
        }
      }).toVector

      Specifications(specs).toJSON(outpath.getParent, Some(outpath.getName))
      println()
      println(s"Created ${outpath.getAbsolutePath}.json.") // create logger and make this level INFO
      println()

    } else {
      val outpath: File = if (args.size == 2) new File(args(1)) else new File(".")
      if (!(outpath.exists | outpath.mkdirs)) sys.error("Output location does not exist.")
      if (!outpath.isDirectory) sys.error("Output location is not a folder.")

      val res: Array[(String, Specifications)] = mta.map(m => {
        val lines = sourceFromPath(m).getLines().toVector.map(_.split("\\s+")(0))
        val specs: IndexedSeq[Specification] = lines.filter(_ != "").map(line => {
          val file = matchFile(s"${line}.spc", files, true)
          file match {
            case None => sys.error(s"No spc file matching '${line}' found.")
            case Some(f) => {
              Specification.fromFile(line.split('/').last, f) match {
                case Success(s) => s.resolveFiles(files) match {
                  case Success(t) => t
                  case Failure(e) => sys.error(s"Could not resolve all files for ${f.getName}.")
                }
                case Failure(e) => sys.error(s"Failed to import ${f.getName}.\n\n===\n${e.getMessage}\n===\n\n")
              }
            }
          }
        })
        (m.getName.split('.').head, Specifications(specs))
      })

      res.foreach(x => {
        val name: String = x._1
        val specs: Specifications = x._2
        specs.toJSON(outpath, Some(name))
        println()
        println(s"Created ${outpath.getAbsolutePath}/$name.json.") // create logger and make this level INFO
        println()
      })
    } 
  }
}
