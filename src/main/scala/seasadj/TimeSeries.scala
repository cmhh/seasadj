package org.cmhh.seasadj

import java.io.File

/**
 * Time series frequency
 *
 * @param frequency integer&ndash;one of 1, 4, or 12
 */
case class Frequency(frequency: Int){
  require(List(1,4,12).contains(frequency), s"Invalid frequency ($frequency).")
  override def toString = frequency.toString
}

/**
 * Singleton [[Frequency]] objects.
 */
case object Frequency {
  val ANNUAL = Frequency(1)
  val QUARTERLY = Frequency(4)
  val MONTHLY = Frequency(12)
}

/**
 * Time series.
 *
 * @param data data
 * @param start start date of series
 * @param frequency frequency
 */
case class TimeSeries(data: NumArray, start: Date, frequency: Frequency) extends JSONOutput {
  lazy val dim: (Int, Int) = data.dim
  lazy val size: Int = dim._1
  lazy val dates: IndexedSeq[Date] = this.start.seq(size)
  lazy val years: IndexedSeq[Int] = dates.map(_.year)
  lazy val periods: IndexedSeq[Int] = dates.map(_.period)

  /**
   * Apply a function to each value in time series.
   *
   * @param f mapping from [[Double]] to [[Double]]
   */
  def map(f: Double => Double): TimeSeries = 
    TimeSeries(
      data.map(f),
      start, frequency
    )

  /**
    * Combine time series.
    * 
    * @param that [[TimeSeries]]
    * @param op function {{(Double, Double) => Double}}
    *
    * @return [[TimeSeries]]
    */
  def comb(that: TimeSeries)(op: (Double, Double) => Double) = {
    require(start == that.start, "Series must have matching start dates.")
    require(frequency == that.frequency, "Series must have matching frequency.")
    require(dates(0) == that.dates(0), "Series must cover same range.")
    TimeSeries(
      data.comb(that.data)(op),
      start, frequency
    )
  }

  override def toString: String = {
    val w = data.data.flatMap(x => x).map(x => (math.round(x * 1e8) / 1e8).toString.size).max
    val f = frequency.frequency
    val p = periods.indexOf(1)
    val n = (f - p) % f

    def pad(s: String): String = s"%${w}s".format(s)

    val months = 
      if (f == 4) List("mar", "jun", "sep", "dec")
      else List("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

    if (data.dim._2 == 1) {
      val dates_ = if (n == 0) dates else (dates(0) - n).seq(size + n)
      val txt = data.data.map(x => x match {
        case None => "NA"
        case Some(d) => (math.round(d * 1e8) / 1e8).toString
      })
      val txt_ = if (n == 0) txt else ((1 to n).toVector.map(i => "") ++ txt)
      val hdr = "     " + months.map(pad(_)).mkString(" ")

      val lines = (1 to (dates_.size / f)).map(i => {
        val y = dates_((i - 1) * f).year
        y.toString + " " + txt_.drop((i - 1) * f).take(f).map(pad(_)).mkString(" ")
      }).mkString("\n")

      hdr + "\n" + lines
    } else {
      (1 to data.dim._1).map(i => {
        dates(i - 1).toString + "  " + (1 to data.dim._2).map(j => data(i,j) match {
          case Some(d) => (math.round(d * 1e8) / 1e8).toString
          case None => "NA"
        }).map(pad(_)).mkString("  ")
      }).mkString("\n")
    }
  }

  /**
   * Output JSON string.
   *
   * @param allDates if false, just output start date and frequency
   */
  def toJSON(allDates: Boolean): String = {
    val d = dates.map(x => s""""$x"""")

    val v = if (data.dim._2 == 1) {
      data.data.map(x => x match {
        case Some(x) => x.toString
        case None => ""
      }).mkString(",")
    } else {
      (1 to data.dim._1).map(i => {
        val row = (1 to data.dim._2).map(j => data(i,j) match {
          case Some(x) => x.toString
          case None => ""
        }).mkString(",")
        s"[$row]"
      }).mkString(",")
    }

    if (allDates) 
      s"""{"date":[${d.mkString(",")}],"value":[$v]}"""
    else
      s"""{"start":"${start.toString}","period":${frequency.frequency},"value":[$v]}"""
  }

  /**
   * Output JSON string,
   */
  def toJSON: String = toJSON(true)

  /**
   * Return specification values.
   */
  def toSpecValue: (SpecNumArray, SpecDate) = {
    val d = if (data.dim._2 == 1)
      data.data
    else 
      (1 to data.dim._1).flatMap(i => (1 to data.dim._2).map(j => data(i,j)))
    (SpecNumArray(d: _*), SpecDate(start))
  }

  /**
   * Subset [[TimeSeries]].
   *
   * @param start start date&ndash;if `None` uses `this.start`
   * @param end end date&ndash;if `None` uses `this.start`
   */
  def window(start: Option[Date], end: Option[Date]): TimeSeries = {
    val s: Date = start match {
      case Some(d) => d
      case _ => dates.head
    }

    val e: Date = end match {
      case Some(d) => d
      case _ => dates.last
    }

    val rows = dates.zipWithIndex.filter(x => x._1 >= s & x._1 <= e).map(_._2 + 1)
    val array = NumArray(
      (1 to data.dim._2).flatMap(j => rows.map(i => data(i,j))).toVector,
      Some(rows.size), Some(data.dim._2), false
    )

    TimeSeries(
      array,
      s,
      frequency
    )
  }

  /**
   * Subset [[TimeSeries]]&ndash;from some date to the end of the series.
   *
   * @param date date
   */
  def from(date: Date): TimeSeries = ??? /*window(Some(date), None)*/

  /**
   * Subset [[TimeSeries]]&ndash;from the start of the series to some date.
   *
   * @param date date
   */
  def until(date: Date): TimeSeries = window(None, Some(date))
}

/**
 * Factory methods for [[TimeSeries]].
 */
case object TimeSeries {
  import FileUtils._
  
  def apply[T: Numeric](data: =>IndexedSeq[T], start: Date, frequency: Frequency): TimeSeries = {
    val num = implicitly[Numeric[T]]
    TimeSeries(NumArray(data), start, frequency)
  }

  def fromFile(file: String, skip: Int = 0): TimeSeries = {
    val src = sourceFromPath(file)
    val lines = src.getLines().toVector.drop(skip).map(_.trim.split("\\s+"))
    src.close()

    val n = lines(0).size
    val dateParts = if (lines(0)(0).matches("^\\d{4}$")) 2 else 1

    val dates = lines.map(x => {
      if (dateParts == 2) (x(0).toInt, x(1).toInt)
      else if (x(0).matches("^\\d{4}\\d{2}")) (x(0).take(4).toInt, x(0).takeRight(2).toInt)
      else sys.error("Unhandled format.")
    })

    val data = NumArray(
      lines.flatMap(x => {
        x.drop(dateParts).map(y => Some(y.toDouble))
      }),
      None,
      Some(n - dateParts),
      true
    )

    val year = dates.map(_._1)
    val period = dates.map(_._2)
    
    val f = period.distinct.size
    if (!List(4, 12).contains(f)) sys.error("Series must be monthly or quarterly.")

    if (f == 4)
      TimeSeries(data, Quarter(year(0), period(0)), Frequency.QUARTERLY)
    else
      TimeSeries(data, Month(year(0), period(0)), Frequency.MONTHLY)
  }
  
  def fromFile(file: File, skip: Int): TimeSeries = fromFile(file.getAbsolutePath, skip)
}