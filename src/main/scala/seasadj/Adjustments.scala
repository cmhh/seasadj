package org.cmhh.seasadj

case class Adjustments(adjustments: IndexedSeq[Adjustment]) extends JSONOutput {
  def toJSON: String = {
    val res = adjustments.map(x => {
      s""""${x.name}":${x.toJSON}"""
    })
    s"""{${res.mkString(",")}}"""
  }
}