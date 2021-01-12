package org.cmhh.seasadj

import java.io.{File, BufferedWriter, FileWriter}
import java.nio.file.Path

trait JSONOutput {
  import FileUtils._

  /**
   * Convert to JSON string.
   */
  def toJSON: String

  /**
   * Convert to JSON and write to file.
   *
   * @param folder target folder as [[java.io.File]]
   * @param name optional filename&ndash;a random filename is used otherwise  
   */
  def toJSON(folder: File, name: Option[String] = None): File = {
    val file: File = name match {
      case None => new File(s"${folder.getAbsolutePath}/${getTempFile(folder.getAbsolutePath, prefix = None, suffix = Some(".json"))}")
      case Some(f) => new File(s"${folder.getAbsolutePath}/$f.json")
    }    
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(toJSON)
    bw.close()
    file
  }

  /**
   * Convert to JSON and write to file.
   *
   * @param folder target folder as [[String]]
   * @param name optional filename&ndash;a random filename is used otherwise
   */
  def toJSON(folder: String, name: Option[String]): File = toJSON(new File(folder), name)

  /**
   * Convert to JSON and write to file.
   *
   * @param folder target folder as [[java.nio.Path]]
   * @param name optional filename&ndash;a random filename is used otherwise
   */
  def toJSON(folder: Path, name: Option[String]): File = toJSON(folder.toFile, name)

  /**
   * Convert to JSON and write to file.
   *
   * @param folder target folder as [[String]]
   */
  def toJSON(folder: String): File = toJSON(new File(folder), None)

  /**
   * Convert to JSON and write to file.
   *
   * @param folder target folder as [[java.nio.Path]]
   */
  def toJSON(folder: Path): File = toJSON(folder.toFile, None)
}