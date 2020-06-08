package org.cmhh.seasadj

/**
 * Specification value.
 */
abstract sealed class SpecValue {
  def toString(): String
  def toJSON(): String 
}

/**
 * Boolean specification value.
 */
case class SpecBool(value: Boolean) extends SpecValue {
  override def toString = if (value) "yes" else "no"
  override def toJSON = if (value) """"yes"""" else """"no""""
}

/**
 * Factory methods for [[SpecBool]].
 */
case object SpecBool {
  def apply(value: String, inputType: InputType = STRING): SpecBool = value.toLowerCase match {
    case "true" => SpecBool(true)
    case _ => SpecBool(false)
  }

  val YES = SpecBool(true)
  val NO = SpecBool(false)
}

/**
 * Composite type specification value.
 */
case class SpecCompType(value: String) extends SpecValue {
  require(
    List("none", "add", "sub", "mult", "div").contains(value.toLowerCase),
   "Composite type must be one of 'none', 'add', 'sub', 'mult', or 'div'"
   )

  override def toString = value.toLowerCase
  override def toJSON = s""""${value.toLowerCase}""""
}

case object SpecCompType {
  def apply(value: String, inputType: InputType = STRING): SpecCompType = SpecCompType(value)
  def fromString(value: String): SpecCompType = SpecCompType(value)
  def fromJSON(value: String): SpecCompType = SpecCompType(value)

  val NONE = SpecCompType("none")
  val ADD = SpecCompType("add")
  val SUB = SpecCompType("sub")
  val MULT = SpecCompType("mult")
  val DIV = SpecCompType("div")
}

/**
 * Model order specification value.
 */
case class SpecOrder(nonseasonal: Int, seasonal: Int) extends SpecValue {
  require(List(0,1,2).contains(nonseasonal), "Non-seasonal order must be in the range [0,2].")
  require(List(0,1).contains(seasonal), "Seasonal order must be 0 or 1.")
  override def toString = s"($nonseasonal $seasonal)"
  override def toJSON = s"[$nonseasonal, $seasonal]"
}

/**
 * Factory methods for [[SpecOrder]].
 */
case object SpecOrder {
  def apply(value: String, inputType: InputType = STRING): SpecOrder = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecOrder = {
    val vals = value
      .replace("(", "").replace(")", "")
      .replace(","," ")
      .trim.split("\\s+")
    if (vals.size != 2) sys.error("Invalid input for type SpecDiff: \"$value\".")
    SpecOrder(vals(0).toInt, vals(1).toInt)
  }

  def fromJSON(value: String): SpecOrder = {
    val vals = value
      .replace("[", "").replace("]", "")
      .replace(" ", "")
      .trim.split(',')
    if (vals.size != 2) sys.error("Invalid input for type SpecDiff: \"$value\".")
    SpecOrder(vals(0).toInt, vals(1).toInt)
  }
}

/**
 * String specification value.
 */
case class SpecString(value: String) extends SpecValue {
  override def toString: String = 
    if (value.contains(" ")) s""""${value}"""" else value

  def toJSON: String = s""""${value}""""
}

/**
 * Factory methods for [[SpecString]].
 */
case object SpecString {
  def apply(value: String, inputType: InputType = STRING): SpecString = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }
  def fromString(value: String): SpecString = SpecString(value.trim)

  def fromJSON(value: String): SpecString = {
    val v = value.trim
    if (v.take(1) == "\"" & v.takeRight(1) == "\"") SpecString(v.drop(1).dropRight(1))
    else SpecString(v)
  }
}

/**
 * String array specification value.
 */
case class SpecStringArray(values: Option[String]*) extends SpecValue {
  override def toString: String = {
    val str = values
      .map(value => value match {
        case Some(s) => if (s.contains(" ")) s""""${s}"""" else s
        case None => ""
      })
      .mkString(", ")
    if (values.size == 1) str
    else s"($str)"
  }

  def toJSON: String = {
    val str = values
      .map(value => value match {
        case Some(s) => s""""$s""""
        case None => "null"
      })
    s"[$str]"
  }
}

/**
 * Factory methods for [[SpecStringArray]].
 */
case object SpecStringArray {
  def apply(values: =>Seq[Option[String]]): SpecStringArray = SpecStringArray(values: _*)
  def create(values: String*): SpecStringArray = SpecStringArray(values.map(v => Some(v)): _*)
  def create(values: =>Seq[String]): SpecStringArray = SpecStringArray(values.map(v => Some(v)): _*)

  def apply(value: String, inputType: InputType = STRING): SpecStringArray = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecStringArray = {
    def opt(s: String): Option[String] = if (s.trim.size == 0) None else Some(s.trim)

    @scala.annotation.tailrec
    def fromString_(
      s: List[Char], numdquote: Int, numsquote: Int, comma: Boolean, buffer: String, accum: List[Option[String]]
    ): List[Option[String]] = s match {
      case Nil => if (!comma & buffer.trim == "") accum else accum :+ opt(buffer)
      case h::t => {
        val outside = numdquote % 2 == 0 & numsquote % 2 == 0
        if ((h == '(' | h == ')') & outside) 
          fromString_(t, numdquote, numsquote, comma, buffer, accum)
        else if (h == '\n' | h == '\r')
          fromString_(t, numdquote, numsquote, comma, buffer, accum)
        else if (h == ',' & outside)
          fromString_(t, numdquote, numsquote, true, "", accum :+ opt(buffer))
        else if (h == ' ' & outside) {
          if (buffer.trim != "")
            fromString_(t, numdquote, numsquote, false, "", accum :+ opt(buffer))
          else
            fromString_(t, numdquote, numsquote, comma, buffer, accum)
        }
        else if (h == '"' & numsquote % 2 == 0) {
          fromString_(t, numdquote + 1, numsquote, false, buffer, accum)
        }
        else if (h == '\'' & numdquote % 2 == 0) {
          fromString_(t, numdquote, numsquote + 1, false, buffer, accum)
        }
        else
          fromString_(t, numdquote, numsquote, comma, buffer + h, accum)
      }
    }
    SpecStringArray(fromString_(value.toList, 0, 0, false, "", List[Option[String]]()): _*)
  }

  def fromJSON(value: String): SpecStringArray = {
    def fromJSON_(s: List[Char], numquote: Int, buffer: String, accum: Array[Option[String]]): Array[Option[String]] = s match {
      case Nil => accum
      case h::t => {
        if (h == '[' & (numquote % 2 == 0))
          fromJSON_(t, numquote, buffer, accum)
        else if ((h == ',' | h == ']') & (numquote % 2 == 0))
          fromJSON_(t, numquote, "", if (buffer.trim.size == 0) accum else accum :+ Some(buffer.trim))
        else if (h == ' ' & (numquote % 2 == 0))
          fromJSON_(t, numquote, buffer, accum)
        else if (h == '"')
          fromJSON_(t, numquote + 1, buffer, accum)
        else if (numquote % 2 == 0)
          if (h == 'n' & t.take(3) == List('u', 'l', 'l')) 
            fromJSON_(t.drop(3), numquote, "", accum :+ None)
          else 
            sys.error("Invalid input.")
        else
          fromJSON_(t, numquote, buffer + h, accum)
      }
    }
    SpecStringArray(fromJSON_(value.toList, 0, "", Array[Option[String]]()).toList: _*)
  }
}

/**
 * Integer specification value.
 */
case class SpecInt(value: Int) extends SpecValue {
  override def toString: String = value.toString
  def toJSON: String = toString
}

/**
 * Factory methods for [[SpecInt]].
 */
case object SpecInt {
  def apply(value: String, inputType: InputType = STRING): SpecInt = SpecInt(value.toInt)
  def fromString(value: String): SpecInt = SpecInt(value.toInt)
  def fromJSON(value: String): SpecInt = SpecInt(value.toInt)
}

/**
 * Numeric (float) specification value.
 */
case class SpecNum(value: Double) extends SpecValue {
  override def toString: String = value.toString
  def toJSON: String = toString
}

/**
 * Factory methods for [[SpecNum]].
 */
case object SpecNum {
  def apply(value: String, inputType: InputType = STRING): SpecNum = SpecNum(value.toDouble)
  def fromString(value: String): SpecNum = SpecNum(value.toDouble)
  def fromJSON(value: String): SpecNum = SpecNum(value.toDouble)
}

/**
 * Numeric array specification value.
 */
case class SpecNumArray(values: Option[Double]*) extends SpecValue {
  override def toString: String = {
    val nonEmpty = values.flatMap(x => x)

    val wLeft = nonEmpty.map(x => {
      if (!x.toString.contains(".")) x.toString.size
      else (x.toString.split('.'))(0).size
    }).max

    val wRight = nonEmpty.map(x => {
      if (!x.toString.contains(".")) 0
      else (x.toString.split('.'))(1).size
    }).max

    val linesize: Int = math.min(12, math.floor(60 / (wLeft + wRight + 3)).toInt)

    val fmt =
      if (wRight == 0) s"%${wLeft}d"
      else s"%${wLeft + wRight + 1}.${wRight}f"

    val w =
      if (wRight == 0) wLeft
      else wLeft + wRight + 1

    @scala.annotation.tailrec
    def toString_(vals: List[Option[Double]], n: Int, accum: String): String = vals match {
      case Nil => accum
      case h::t => {
        val s: String = h match {
          case Some(x) =>
            if (wRight == 0) fmt.format(x.toInt)
            else fmt.format(x)
          case None =>
            (1 to w).map(i => " ").mkString
        }
        val add: String = if ((n > 0) & ((n + 1) % linesize == 1)) {
          s",\n $s"
        } else {
          if (n > 0) s", $s" else s
        }
        toString_(t, n + 1, accum + add)
      }
    }

    "(" + toString_(values.toList, 0, "") + ")"
  }

  def toJSON: String = {
    "[" + values.map(value => value match {
      case Some(x) => x.toString
      case None => "null"
    }).mkString(", ") + "]"
  }
}

/**
 * Factory methods for [[SpecNumArray]].
 */
case object SpecNumArray {
  def apply[T: Numeric](values: T*): SpecNumArray = {
    val num = implicitly[Numeric[T]]
    val vals: Seq[Option[Double]] = values.map(value => Some(num.toDouble(value)))
    SpecNumArray(vals: _*)
  }

  def apply[T: Numeric](values: =>Seq[Option[T]]): SpecNumArray = {
    val num = implicitly[Numeric[T]]
    val vals: Seq[Option[Double]] = values.map(value => value match {
      case Some(x) => Some(num.toDouble(x))
      case _ => None
    })
    SpecNumArray(vals: _*)
  }

  def apply(value: String, inputType: InputType = STRING): SpecNumArray = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecNumArray = {
    val val_ = value
      .replaceAll("[(]\\s+", "(").replaceAll("\\s+[)]", ")").replaceAll("\\s+", " ")
    new SpecNumArray(
      val_.split("\\s+").flatMap(s => s.split(',')).toIndexedSeq.map(x => {
        val y = x.trim.replace("(","").replace(")","")
        y match {
          case "" => None
          case _ => Some(y.toDouble)
        }
      }): _*
    )
  }

  def fromJSON(value: String): SpecNumArray = {
    new SpecNumArray(
      value
        .replace("[","").replace("]","")
        .replaceAll("\\s+", "")
        .split(',')
        .map(v => v.toLowerCase match {
          case "null" => None
          case _ => Some(v.toDouble)
        }).toIndexedSeq: _*
    )
  }
}

/**
 * Span specification value.
 */
case class SpecSpan(start: Option[Date], end: Option[Date]) extends SpecValue {
  require(!(start == None & end == None), "start and end cannot both be empty.")
  
  override def toString: String = {
    def str(s: Option[Date]): String = s match {
      case Some(d) => d.toString
      case _ => ""
    }
    s"(${str(start)}, ${str(end)})"
  }
  def toJSON: String = {
    def str(s: Option[Date]): String = s match {
      case Some(d) => d.toString
      case _ => "null"
    }
    s"[${str(start)}, ${str(end)}]"
  }
}

/**
 * Factory methods for [[SpecSpan]].
 */
case object SpecSpan {
  def apply(value: String, inputType: InputType = STRING): SpecSpan = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecSpan = {
    val x: IndexedSeq[String] = value.split(',').toVector
    if (x.size != 2) sys.error("Invalid input for type SpecSpan.")
    val y: IndexedSeq[Option[Date]] = x
      .map(_.trim.replace("(", "").replace(")",""))
      .map(z => {
        z match {
          case "" => None
          case _ => Some(Date(z))
        }
      })
    SpecSpan(y(0), y(1))
  }

  def fromJSON(value: String): SpecSpan = {
    val x = value
      .replace("[","").replace("]","")
      .split(',')
      .map(_.trim)
      .map(d => d.toLowerCase match {
        case "null" => None
        case _ => Some(Date(d))
      })
    if (x.size != 2) sys.error("Invalid input for type SpecSpan.")
    SpecSpan(x(0), x(1))
  }
}

/**
 * Date specification value.
 */
case class SpecDate(value: Date) extends SpecValue {
  override def toString: String = value.toString
  def toJSON: String = toString
}

/**
 * Factory methods for [[SpecDate]].
 */
case object SpecDate {
  def apply(value: String, inputType: InputType = STRING): SpecDate = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecDate = SpecDate(Date(value))
  def fromJSON(value: String): SpecDate = SpecDate(Date(value))
}

/**
 * ARMA specification value.
 */
case class SpecARMA(value: List[Coefficient]) extends SpecValue {
  override def toString: String = s"(${value.mkString(", ")})"
  def toJSON: String = s"[${value.mkString(", ")}]"
}

/**
 * Factory methods for [[SpecARMA]].
 */
case object SpecARMA {
  def apply(value: String, inputType: InputType = STRING): SpecARMA = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecARMA = {
    SpecARMA(
      value
        .split("(,|\\s)+")
        .map(_.replace("(","").replace(")",""))
        .map(Coefficient.fromString(_))
        .toList
    )
  }

  def fromJSON(value: String): SpecARMA = {
    SpecARMA(
      value
        .replace("[", "").replace("]","")
        .split("(,|\\s+)")
        .map(c => c.trim.toLowerCase match {
          case "null" => MissingCoefficient
          case _ => Coefficient.fromString(c)
        })
        .toList
    )
  }
}

/**
 * ARIMA specification value.
 */
case class SpecARIMA(pdq: Model, PDQ: Option[Model], L: Option[Int]) extends SpecValue {
  require(numParams <= 133)

  override def toString: String = {
    PDQ match {
      case Some(m) => L match {
        case Some(i) => s"$pdq$m$i"
        case _ => s"$pdq$m"
      }
      case _ => s"$pdq"
    }
  }

  def toJSON: String = toString

  def numParams: Int = PDQ match {
    case None => pdq.numParams
    case Some(m) => pdq.numParams + m.numParams
  }
}

/**
 * Factory methods for [[SpecARIMA]].
 */
case object SpecARIMA {
  def apply(value: String, inputType: InputType = STRING): SpecARIMA = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecARIMA = {
    def parse(s: List[Char], numbracket: Int, buffer: String, accum: List[String]): List[String] = s match {
      case Nil => accum :+ buffer
      case h::t => {
        if ((h == ' ' | h == ',') & numbracket % 2 == 0)
          parse(t, numbracket, "", accum :+ buffer)
        else if (h == '[' | h == ']')
          parse(t, numbracket + 1, buffer, accum)
        else
          parse(t, numbracket, buffer + h, accum)
      }
    }

    def toModel(value: String): Model = {
      val m = parse(value.toList, 0, "", List[String]())
      Model(
        Order(m(0).split("(,|\\s)+").map(_.toInt).toList),
        m(1).toInt,
        Order(m(2).split("(,|\\s)+").map(_.toInt).toList)
      )
    }

    val x = value.split(')').map(_.replace("(",""))

    SpecARIMA(
      toModel(x(0)),
      if (x.size > 1) Some(toModel(x(1))) else None,
      if (x.size == 3) Some(x(2).toInt) else None
    )
  }

  def fromJSON(value: String): SpecARIMA = fromString(value)
}