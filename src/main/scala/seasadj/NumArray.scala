package org.cmhh.seasadj

/**
  * Basic 2-dimensional array.
  *
  * @param data input as 1-d vector
  * @param nrow row dimension
  * @param ncol column dimension
  * @param byrow whether data is stored by-row or not (default is {{false}})
  */
case class NumArray(
  data: IndexedSeq[Option[Double]], 
  nrow: Option[Int] = None, ncol: Option[Int] = None, byrow: Boolean = false
) {
  val dim: (Int, Int) = nrow match {
    case Some(m) => ncol match {
      case Some(n) => (m, n)
      case None => (m, data.size / m)
    }
    case None => ncol match {
      case Some(n) => (data.size / n, n)
      case None => (data.size, 1)
    }
  }

  require(dim._1 * dim._2 == data.size, "Incompatible dimensions.")

  override def toString: String = {
    val w = data.flatMap(x => x).map(_.toString.size).max
    val fmt = s"%${w}s"
    (1 to dim._1).map(i => {
      (1 to dim._2).map(j => {
        apply(i,j) match {
          case Some(x) => fmt.format(x)
          case None => fmt.format("")
        }
      }).mkString(" ")
    }).mkString("\n")
  }

  /**
    * Extract element by index.
    *
    * @param i row position
    * @param j column position
    * @return [[Option[Double]]]
    */
  def apply(i: Int, j: Int): Option[Double] = {
    if (i < 1 | j < 0 | i > dim._1 | j > dim._2) sys.error("Index out of bounds.")

    if (!byrow) {
      data((j - 1) * dim._1 + i - 1)
    } else {
      data((i - 1) * dim._2 + j - 1)
    }
  }


  /**
    * Extract single column.
    *
    * @param j column position
    * @return [[IndexedSeq[Option[Double]]]]
    */
  def apply(j: Int): IndexedSeq[Option[Double]] = {
    (1 to dim._1).map(i => apply(i,j))
  }

  /**
    * Apply function to elements of array.
    *
    * @param f function
    * @return [[NumArray]]
    */
  def map(f: Double => Double): NumArray = {
    NumArray(
      data.map(
        x => x match {
          case Some(n) => Some(f(n))
          case _ => None
        }
      ),
      Some(dim._1),
      Some(dim._2),
      byrow
    )
  } 

  /**
    * Combine comforming arrays.
    *
    * @param that array to combine
    * @param op [[(Double, Double)=>(Double)]]
    * @return [[NumArray]]
    */
  def comb(that: NumArray)(op: (Double, Double) => Double): NumArray = {
    require(dim._1 == that.dim._1 && dim._2 == that.dim._2, "Incompatible dimensions")
    NumArray(
      for {
        j <- 1 to dim._2
        i <- 1 to dim._1
      } yield this(i,j).flatMap(x => that(i,j).map(y => op(x, y))),
      Some(dim._1),
      Some(dim._2),
      false
    )
  }
}

/**
  * Factory methods.
  */
case object NumArray {
  def apply[T: Numeric](data: IndexedSeq[T], nrow: Int, ncol: Int): NumArray = {
    val num = implicitly[Numeric[T]]
    NumArray(data.map(x => Some(num.toDouble(x))), Some(nrow), Some(ncol), false)
  }

  def apply[T: Numeric](data: IndexedSeq[T], ncol: Int): NumArray = {
    val num = implicitly[Numeric[T]]
    NumArray(data.map(x => Some(num.toDouble(x))), None, Some(ncol), false)
  }

  def apply[T: Numeric](data: IndexedSeq[T]): NumArray = {
    val num = implicitly[Numeric[T]]
    NumArray(data.map(x => Some(num.toDouble(x))), None, None, false)
  }
}