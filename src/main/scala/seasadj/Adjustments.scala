package org.cmhh.seasadj

case class Adjustments(adjustments: IndexedSeq[Adjustment]) extends JSONOutput {
  /**
   * Subset [[Adjustments]].
   *
   * @param f mapping from [[Adjustment]] to [[Boolean]]
   */
  def filter(f: Adjustment => Boolean): Adjustments = 
    Adjustments(adjustments.filter(f))

  /**
   * Convert to JSON string.
   *
   * @param allDates whether to include dates in full for time series outputs, or just start and frequency
   */
  def toJSON(allDates: Boolean): String = {
    val res = adjustments.map(x => {
      s""""${x.name}":${x.toJSON(allDates)}"""
    })
    s"""{${res.mkString(",")}}"""
  }

  /**
   * Convert to JSON string.
   */
  def toJSON: String = toJSON(true)
}