package org.cmhh.seasadj

/**
 * Specification value.
 */
abstract sealed class SpecValue {
  def toString: String
  def toJSON: String 
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
   s"""Composite type must be one of 'none', 'add', 'sub', 'mult', or 'div' (got '${value.toLowerCase}')."""
   )

  override def toString = value.toLowerCase
  override def toJSON = s""""${value.toLowerCase}""""
}

case object SpecCompType {
  def apply(value: String, inputType: InputType = STRING): SpecCompType = inputType match {
    case STRING => fromString(value)
    case JSON => fromJSON(value)
  }

  def fromString(value: String): SpecCompType = SpecCompType(value)
  def fromJSON(value: String): SpecCompType = SpecCompType(StringUtils.unquote(value))

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
    val vals = StringUtils.unquote(value)
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

  def fromJSON(value: String): SpecString = SpecString(StringUtils.unquote(value.trim))
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
      .mkString(",")
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

  /**
    * Create array of [[String]]s from [[String]]
    * 
    * Hot mess.  Simplify using regex at some point.
    *
    * @param value input
    * @return [[org.cmhh.seasadj.SpecStringArray]]
    */
  def fromString(value: String): SpecStringArray = {
    val v = value
      .trim
      .replaceAll(",\\s*,", ", NULL,")
      .replaceAll(",\\s*\\)", ", NULL\\)")

    val quoted = """(?:'[a-zA-Z0-9]+[a-zA-Z0-9", -]*')|(?:"[a-zA-Z0-9]+[a-zA-Z0-9', -]*")"""
    val nonquoted = "[a-zA-Z0-9]+[a-zA-Z0-9-]*"
    val ok = s"""(?:$quoted)|(?:$nonquoted)"""

    if (v.take(1) != "(" | v.takeRight(1) != ")")
      if (v.matches(quoted)) SpecStringArray(StringUtils.unquote(v))
      else throw new IllegalArgumentException(s"String cannot be parsed into SpecStringArray ('${value}').")
    else {
      val arr = ok.r.findAllIn(v).toList.map(s => StringUtils.unquote(s)).map(s => if (s == "NULL") None else Some(s))
      SpecStringArray(arr: _*)
    }
  }

  /**
    * Create array of [[String]]s from JSON string
    * 
    * Hot mess.  Simplify using regex / ujson.read at some point.
    *
    * @param value input
    * @return [[org.cmhh.seasadj.SpecStringArray]]
    */
  def fromJSON(value: String): SpecStringArray = {
    import upickle.default._

    val v = value.trim    
    val quoted = """(?:'[a-zA-Z0-9]+[a-zA-Z0-9" -]*')|(?:"[a-zA-Z0-9]+[a-zA-Z0-9' -]*")"""

    if (v.take(1) != "[" | v.takeRight(1) != "]")
      if (v.matches(quoted)) SpecStringArray(StringUtils.unquote(v))
      else throw new IllegalArgumentException(s"String cannot be parsed into SpecStringArray ('${value}').")
    else {
      ujson.read(v) match {
        case a: ujson.Arr => 
          val arr = a.arr.toList.map(x => x match {
            case ujson.Null => None
            case s: ujson.Str => Some(s.str)
            case _ => throw new IllegalArgumentException(s"String cannot be parsed into SpecStringArray ('${value}').")
          })
          SpecStringArray(arr: _*)
        case _ => throw new IllegalArgumentException(s"String cannot be parsed into SpecStringArray ('${value}').")
      }
    }
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
  def fromJSON(value: String): SpecInt = SpecInt(StringUtils.unquote(value.trim).toInt)
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
  def fromJSON(value: String): SpecNum = SpecNum(StringUtils.unquote(value.trim).toDouble)
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
      StringUtils.unquote(value.trim)
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

  require(
    {
      start match {
        case Some(s) => {
          end match {
            case Some(e) => (s.frequency == e.frequency) & (e >= s)
            case _ => true
          }
        }
        case _ => true
      }
    }, 
    "start and end must have same frequency, and end must be after start."
  )
  
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
  def apply(start: Date):SpecSpan = SpecSpan(Some(start), None)
  def apply(start: Date, end: Date): SpecSpan = SpecSpan(Some(start), Some(end))

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
    val x = StringUtils.unquote(value.trim)
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
  def fromJSON(value: String): SpecDate = SpecDate(Date(StringUtils.unquote(value.trim)))
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
    val split = 
      if (value.contains(",")) 
        value.replaceAll("\\s+", "").split(",") 
      else
        value.split("\\s+")
    SpecARMA(
      split
        .map(_.replace("(","").replace(")",""))
        .map(Coefficient.fromString(_))
        .toList
    )
  }

  def fromJSON(value: String): SpecARMA = {
    SpecARMA(
      StringUtils.unquote(value.trim)
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
case class SpecARIMA(value: List[Model]) extends SpecValue {
  val numParams: Int = value.map(pdq => pdq.numParams).sum
  require(numParams <= 133)

  override def toString: String = value.map(pdq => pdq.toString).mkString("")

  def toJSON: String = s""""$toString""""
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
    val o = "((?:\\[(?:\\d+[,\\s]*)+\\])|\\d+)"
    val r = s"\\s*\\(\\s*${o}[,\\s]+${o}[,\\s]+${o}\\s*\\)(\\d*)\\s*".r
    SpecARIMA(r.findAllIn(value).toList.map(Model.fromString))
  }

  def fromJSON(value: String): SpecARIMA = fromString(StringUtils.unquote(value.trim).replaceAll("\"", ""))
}