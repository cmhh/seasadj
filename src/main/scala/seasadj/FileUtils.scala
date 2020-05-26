package org.cmhh.seasadj

import scala.io.{ BufferedSource, Source }
import java.io.{ BufferedReader, File, InputStreamReader }
import java.nio.file.{ FileVisitResult, Files, Path, Paths, SimpleFileVisitor }
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException

/**
 * Miscellaneous methods for working with files and directories.
 */
object FileUtils {
  /**
   * Assign a temporary folder.
   */
  def getTempFolder: Path = Files.createTempDirectory("x13_")

  /**
   * Assign a temporary file.
   *
   * @param folder folder as [[java.nio.file.Path]]
   * @param prefix optional prefix
   * @param suffix optional suffix&ndash;usually a file extension
   */
  def getTempFile(folder: Path, prefix: Option[String], suffix: Option[String]): Path = {
    (prefix, suffix) match {
      case (Some(p), Some(s)) => Files.createTempFile(folder, p, s)
      case (Some(p), None) => Files.createTempFile(folder, p, "")
      case (None, Some(s)) => Files.createTempFile(folder, "", s)
      case _ => Files.createTempFile(folder, "", "")
    }
  }

  /**
   * Assign a temporary file.
   *
   * @param folder folder as [[String]]
   * @param prefix optional prefix
   * @param suffix optional suffix&ndash;usually a file extension
   */
  def getTempFile(folder: String, prefix: Option[String], suffix: Option[String]): Path =
    getTempFile(Paths.get(folder), prefix, suffix)

  /**
   * Delete folder, including contents.
   *
   * @param folder folder as [[java.nio.file.Path]]
   */
  def deleteFolder(folder: Path): Unit = {
    Files.walkFileTree(folder, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }

  /**
   * Delete folder, including contents.
   *
   * @param folder folder as [[String]]
   */
  def deleteFolder(folder: String): Unit = deleteFolder(Paths.get(folder))

  /**
   * Return a [[scala.io.BufferedSource]].
   *
   * @param path file as [[String]]
   */
  def sourceFromPath(path: String): BufferedSource =
    Source.fromFile(path)

  /**
   * Return a [[scala.io.BufferedSource]].
   *
   * @param path file as [[java.io.File]]
   */
  def sourceFromPath(path: File): BufferedSource =
    Source.fromFile(path)

  /**
   * Return a [[scala.io.BufferedSource]].
   *
   * @param resource resource as [[String]]
   */
  def sourceFromResource(resource: String): BufferedSource =
    Source.fromInputStream(getClass.getResourceAsStream(resource))

  /**
   * Recursively list all files below a folder.
   * 
   * @param root root folder
   * @param skip list of regular expressions&ndash;matching files and folders are skipped
   */
  def listFiles(root: File, skip: List[String] = List[String]()): IndexedSeq[File] = {
    @scala.annotation.tailrec
    def applySkip(x: IndexedSeq[File], s: List[String]): IndexedSeq[File] = s match {
      case Nil => x
      case h::t => applySkip(x.filter(!_.getAbsolutePath.matches(h)), t)
    }

    def listFiles_(r: File): IndexedSeq[File] = 
      if (!root.isDirectory) Vector[File]()
      else {
        val fileList = applySkip(r.listFiles.toVector, skip)
        fileList.filter(_.isFile) ++ fileList.filter(_.isDirectory).flatMap(listFiles_)
      }

    listFiles_(root)
  }

  /**
   * Return the best match for `file` from `candidates`.
   *
   * @param file file as [[String]]
   * @param candidates list of potential matches for file
   * @param ignoreCase whether or not to do case insensitive match
   */
  def matchFile(file: String, candidates: IndexedSeq[File], ignoreCase: Boolean): Option[File] = {
    @scala.annotation.tailrec
    def numSame(s1: String, s2: String, accum: Int = 0): Int = {
      if (s1.size == 0 | s2.size == 0) accum
      else {
        val m = if (ignoreCase) s1.head.toLower == s2.head.toLower else s1.head == s2.head
        numSame(s1.tail, s2.tail, if (m) accum + 1 else accum)
      }
    }

    val f1 = if (ignoreCase) file.toLowerCase else file
    val f2 = f1.split("/").takeRight(1).head
    val m1 = candidates.filter(x => {
      val name = if (ignoreCase) x.getName.toLowerCase else x.getName
      name == f2
    })

    if (m1.size == 0) None 
    else if (m1.size == 0) Some(m1(0))
    else {
      val scores = m1.map(x => {
        val name = if (ignoreCase) x.getAbsolutePath.toLowerCase else x.getAbsolutePath
        (x, numSame(name.reverse, f1.reverse))
      })
      val maxScore = scores.map(_._2).max
      Some(scores.filter(_._2 == maxScore).map(_._1).head)
    }
  }

  /**
   * Return the best match for `file` from `candidates`.
   *
   * @param file file as [[java.io.File]]
   * @param candidates list of potential matches for file
   * @param ignoreCase whether or not to do case insensitive match
   */
  def matchFile(file: File, candidates: IndexedSeq[File], ignoreCase: Boolean): Option[File] = 
    matchFile(file.getAbsolutePath, candidates, ignoreCase)
}