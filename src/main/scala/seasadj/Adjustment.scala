package org.cmhh.seasadj

import java.io.{File, BufferedWriter, FileWriter}
import java.nio.file.Path

/**
 * Holds the outcome of seasonal adjustment.
 *
 * @param name name of original series / specification
 * @param series map containing time series components such as trend, irregular, seasonal factors, and seasonally adjusted series
 * @param diagnostics map containing various diagnostics and summary statistics
 */
case class Adjustment(
  name: String, series: Map[String, TimeSeries], diagnostics: Map[String, DiagnosticValue]
) extends JSONOutput {
  /**
   * Convert to JSON string.
   *
   * @param allDates whether to include dates in full for time series outputs, or just start and frequency
   */
  def toJSON(allDates: Boolean): String = {
    val dstr = diagnostics.keys.toVector.sorted.map(key => {
      s""""$key":${diagnostics(key).toJSON}"""
    })
    val sstr = series.keys.toVector.sorted.map(key => {
      s""""$key":${series(key).toJSON(allDates)}"""
    })
    s"""{"series":{${sstr.mkString(",")}},"diagnostics":{${dstr.mkString(",")}}}"""
  } 

  /**
   * Convert to JSON string.
   */
  def toJSON: String = toJSON(true)

  def getSeries(name: String): TimeSeries = series(name)
  def getSeriesKeys: Iterable[String] = series.keys
  def getDiagnostic(name: String): DiagnosticValue = diagnostics(name)
  def getDiagnosticKeys: Iterable[String] = diagnostics.keys
}