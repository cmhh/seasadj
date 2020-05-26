package org.cmhh.seasadj

/**
 * Simple date representation.
 */
sealed abstract class Date(val year: Int, val period: Int, val frequency: Int) {
  require(period >= 1 & period <= frequency)
  
  override def toString: String = f"$year.$period%02d"

  def +(x: Int): Date
  def -(x: Int): Date = this + (-x)
  def ==(x: Date): Boolean = year == x.year & period == x.period
  def <(x: Date): Boolean = year < x.year | (year == x.year & period < x.period)
  def <=(x: Date): Boolean = this < x | this == x
  def >(x: Date): Boolean = !(this <= x)
  def >=(x: Date): Boolean = this > x | this == x

  /**
   * Generate a sequence of dates, beginning at `this`.
   *
   * @param n size of sequence
   */
  def seq(n: Int): IndexedSeq[Date] = {
    def seq_(n: Int, accum: IndexedSeq[Date]): IndexedSeq[Date] =
      if (n <= 1) accum
      else seq_(n - 1, accum :+ (accum.last + 1))
    seq_(n, Vector(this))
  }
}

/**
 * Concrete month type.
 *
 * @param year year
 * @param period period, an integer from 1 to 12, inclusive
 */
case class Month(override val year: Int, override val period: Int)
  extends Date(year, period, frequency = 12) {
  /**
   * Increment month, or decrement if argument is negative.
   *
   * @param x number of months to add 
   */
  def +(x: Int): Month =
    if (x >= 0)
      new Month(year + ((period + x - 1) / frequency), (period + x - 1) % frequency + 1)
    else {
      val d = math.abs(x) / frequency
      val r = math.abs(x) - d * frequency
      val y = d + (if (r > period) 1 else 0)
      new Month(
        year - y,
        (period + x + y * frequency - 1) % frequency + 1
      )
    }
}

/**
 * Concrete quarter type.
 *
 * @param year year
 * @param period period, and integer from 1 to 4, inclusive.
 */
case class Quarter(override val year: Int, override val period: Int)
  extends Date(year, period, frequency = 4) {
  /**
   * Increment quarter, or decrement if argument is negative.
   *
   * @param x number of quarters to add
   */
  def +(x: Int): Quarter =
    if (x >= 0)
      new Quarter(year + ((period + x - 1) / frequency), (period + x - 1) % frequency + 1)
    else {
      val d = math.abs(x) / frequency
      val r = math.abs(x) - d * frequency
      val y = d + (if (r > period) 1 else 0)
      new Quarter(
        year - y,
        (period + x + y * frequency - 1) % frequency + 1
      )
    }
}

/**
 * Factory methods for [[Date]].
 */
case object Date {
  /**
   * Create [[Date]] from string.
   *
   * The type of date will be inferred from the string.  Strings like `2001.1` will be assumed to be quarterly,
   * while strings like `2001.01` and `2001.jan` will be assumed to be monthly. 
   *
   * @param date date as string.
   */
  def apply(date: String): Date = {
    val split = date.split('.')
    if (split.size != 2) sys.error("Unknown date format.")
    val (year, period) = (split(0).toInt, split(1))
    val months = Vector("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
    if (months.contains(period.toLowerCase)) {
      val pd = months.zipWithIndex.filter(_._1 == period.toLowerCase).map(_._2).head
      Month(year, pd)
    } else if ((1 to 12).map(i => "%02d".format(i)).contains(period)) {
      Month(year, period.toInt)
    } else if ((1 to 4).map(_.toString).contains(period)) {
      Quarter(year, period.toInt)
    } else
      sys.error("Unknown date format.")
  }
}