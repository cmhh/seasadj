package org.cmhh.seasadj

/**
 * AR or MA order, i.e. p, q, P, or Q.
 *
 * Can be a single integer, or an ascending sequence, such as `3,2` or `3,2,1`.
 *
 * @param value A list of integers
 */
case class Order(value: List[Int]) {
  require(increasing(value), "Values must be increasing.")
  require(positive(value), "Values must be positive.")
 
  override def toString: String = {
    if (value.size == 1) value(0).toString
    else {
      s"[${value.mkString(" ")}]"
    }
  }

  /**
   * Total number of parameters.
   */
  val numParams: Int = if (value.size > 1) value.size else value(0)

  @scala.annotation.tailrec
  private def increasing(x: Seq[Int]): Boolean = {
    if (x.size == 1) true
    else if (x(1) < x(0)) false
    else increasing(x.drop(1))
  }

  private def positive(x: Seq[Int]): Boolean = 
    x.filter(_ < 0).size == 0
}

/**
 * Factory methods for [[Order]]
 */
case object Order {
  def apply(value: Int): Order = new Order(List(value))
  def apply(value: Int*): Order = new Order(value.toList)

  def fromString(value: String): Order = {
    val l = value
      .replace("[", "").replace("]", "")
      .trim
      .replaceAll("\\s*,\\s*", " ")
      .replaceAll("\\s+", " ")
      .split(" ")
      .map(_.toInt)
      .toList

    Order(l)
  }

  def fromJSON(value: String): Order = fromString(value.replaceAll("\"", ""))
}

/**
 * ARIMA model.
 *
 * @param p autoregressive order
 * @param d integration order
 * @param q moving average order
 */
case class Model(p: Order, d: Int, q: Order, L: Option[Int]) {
  require(d >= 0 & d <= 3, "Order of integration must be in the range [0,3].")
  require(p.value.max <= 36, "Autoregressive order must be less than or equal to 36.")
  require(q.value.max <= 36, "Moving average order must be less than or equal to 36.")

  override def toString: String = L match {
    case Some(l) => s"($p $d $q)$l"
    case None => s"($p $d $q)"
  }

  /**
   * Total number of paramters.
   */
  val numParams: Int = p.numParams + d + q.numParams
}

case object Model {
  def apply(p: Int, d: Int, q: Int, L: Int): Model = Model(Order(p), d, Order(q), Some(L))
  def apply(p: Int, d: Int, q: Int): Model = Model(Order(p), d, Order(q), None)
  def apply(p: Order, d: Int, q: Order): Model = Model(p, d, q, None)

  def fromString(value: String): Model = {
    // ([1 2] 1 [1 2]), (1, 0, 1), etc.
    val o = "((?:\\[(?:\\d+[,\\s]*)+\\])|\\d+)"
    val r = s"^\\s*\\(\\s*${o}[,\\s]+${o}[,\\s]+${o}\\s*\\)(\\d*)\\s*$$".r
    val r(p, d, q, l) = value

    Model(
      Order.fromString(p),
      d.toInt,
      Order.fromString(q),
      if (l == "") None else Some(l.toInt)
    )
  }

  def fromJSON(value: String): Model = fromString(value.replaceAll("\"", ""))
}

/**
 * AR / MA coefficients.
 */
abstract sealed class Coefficient

/**
 * AR / MA coefficients.
 *
 * @param value value
 * @param fixed fixed&ndash;has the effect of appending `f` when viewed as a string
 */
case class NumericCoefficient(value: Double, fixed: Boolean = false)
  extends Coefficient {
  override def toString: String =
    if (fixed) s"${value.toString}f" else value.toString
}

/**
 * Factory methods for [[NumericCoefficient]].
 */
case object NumericCoefficient {
  def fromString(value: String): NumericCoefficient = {
    val fixed = value.contains('f')
    NumericCoefficient(value.replace("f","").toDouble, fixed)
  }
}

/**
 * Singleton empty AR / MA coefficient.
 */
case object MissingCoefficient extends Coefficient {
  override val toString = ""
}

/**
 * Factory methods for [[Coefficient]]
 */
case object Coefficient {
  def fromString(value: String): Coefficient = {
    if (value.replace(" ", "") == "") MissingCoefficient
    else NumericCoefficient.fromString(value)
  }
}