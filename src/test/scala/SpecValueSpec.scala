package org.cmhh.seasadj

import org.scalatest._
import java.lang.IllegalArgumentException

class SpecValueSpec extends UnitSpec {  
  "SpecBool" can "be created from a Boolean" in {
    val t = SpecBool(true)
    val f = SpecBool(false)

    assert(t.value == true)
    assert(f.value == false)
  }

  it can "be created from a String" in {
    val t = SpecBool("true")
    val f = SpecBool("false")

    assert(t == SpecBool.YES)
    assert(f == SpecBool.NO)
  }

  "SepCompType" can "be create from a String" in {
    assert(SpecCompType("NoNe").toString == SpecCompType.NONE.toString)
  }

  it should "fail for values other than none, add, sub, mult, or div" in {
    assertThrows[IllegalArgumentException]{SpecCompType("other")} 
  }

  "SpecOrder" can "be created from a pair of integers" in {
    assert(SpecOrder(1,1).toString == "(1 1)")
    assertThrows[IllegalArgumentException]{SpecOrder(-1,1)}
    assertThrows[IllegalArgumentException]{SpecOrder(1,-1)}
    assertThrows[IllegalArgumentException]{SpecOrder(3, 1)}
    assertThrows[IllegalArgumentException]{SpecOrder(1, 2)}
  }

  it can "be created from a String" in {
    assert(SpecOrder("(1 1)") == SpecOrder(1, 1))
  }

  it can "be create from JSON" in {
    assert(SpecOrder("[1,1]", JSON) == SpecOrder(1, 1))
  }

  "SpecString" can "be created from a String" in {
    assert(SpecString("foo").value == "foo")
  }

  it can "be created from JSON" in {
    assert(SpecString(""""foo"""", JSON).value == "foo")
  }

  "SpecStringArray" can "be created from a String" in {
    assert(SpecStringArray.fromString("(msr )").values.toList  == List(Some("msr")))
    assert(SpecStringArray.fromString("( msr)").values.toList  == List(Some("msr")))
    assert(SpecStringArray.fromString("( msr )").values.toList == List(Some("msr")))
    assert(SpecStringArray.fromString("(a,b)").values.toList   == List(Some("a"), Some("b")))
    assert(SpecStringArray.fromString("(a b)").values.toList   == List(Some("a"), Some("b")))
    assert(SpecStringArray.fromString("(a,)").values.toList    == List(Some("a"), None))
    assert(SpecStringArray.fromString("('a',)").values.toList  == List(Some("a"), None))
  }

  it can "be created from JSON" in {
    assert(SpecStringArray.fromJSON("""["msr" ]""").values.toList   == List(Some("msr")))
    assert(SpecStringArray.fromJSON("""[ "msr"]""").values.toList   == List(Some("msr")))
    assert(SpecStringArray.fromJSON("""[ "msr" ]""").values.toList  == List(Some("msr")))
    assert(SpecStringArray.fromJSON("""["a","b"]""").values.toList  == List(Some("a"), Some("b")))
    assert(SpecStringArray.fromJSON("""["a",null]""").values.toList == List(Some("a"), None))
  }

  "SpecInt" can "be created from an Int" in {
    assert(SpecInt(1).value == 1)
  }

  it can "be created from a String" in {
    assert(SpecInt("1").value == 1)
  }

  "SpecNum" can "be created from a number" in {
    assert(SpecNum(1).value   == 1.0)
    assert(SpecNum(1.0).value == 1.0)
  }

  it can "be created from a String" in {
    assert(SpecNum("1").value   == 1.0)
    assert(SpecNum("1.0").value == 1.0)
  }

  "SpecNumArray" can "be created from a number sequence" in {
    assert(SpecNumArray(1, 2).values == List(Some(1.0), Some(2.0)))
    assert(SpecNumArray(1.0, 2.0).values == List(Some(1.0), Some(2.0)))
  }

  it can "be created from a String" in {
    assert(SpecNumArray.fromString("(1,)").values  == List(Some(1.0), None))
    assert(SpecNumArray.fromString("(1,2)").values == List(Some(1.0), Some(2.0)))
  }

  it can "be created from JSON" in {
    assert(SpecNumArray.fromJSON("[1,null]").values  == List(Some(1.0), None))
    assert(SpecNumArray.fromJSON("[1,2]").values == List(Some(1.0), Some(2.0)))
  }

  "SpecSpan" can "be created from Dates" in {
    val q1 = Quarter(1986, 1)
    val q2 = Quarter(1996, 4)
    val m1 = Month(2020, 1)
    val m2 = Month(2020, 6)
    val s1 = SpecSpan(q1, q2)
    val s2 = SpecSpan(m1, m2)
    assert(s1.start == Some(q1))
    assert(s1.end == Some(q2))
    assert(s2.start == Some(m1))
    assert(s2.end == Some(m2))
    assert(SpecSpan(q1).end == None)
    assert(SpecSpan(None, Some(q2)).start == None)
  }
  
  it can "be created from a String" in {
    val s1 = SpecSpan("(1986.01,)")
    val s2 = SpecSpan("(,2020.01)")
    val s3 = SpecSpan("(1986.01,2020.01")
    assert(s1.start == Some(Month(1986, 1)))
    assert(s1.end == None)  
    assert(s2.start == None)
    assert(s2.end == Some(Month(2020, 1)))
    assert(s3.start == Some(Month(1986, 1)))
    assert(s3.end == Some(Month(2020, 1)))
  }
  
  it can "be created from JSON" in {
    val s1 = SpecSpan.fromJSON("[1986.01, Null]")
    val s2 = SpecSpan.fromJSON("[Null, 2020.01]")
    val s3 = SpecSpan.fromJSON("[1986.01, 2020.01]")
    assert(s1.start == Some(Month(1986, 1)))
    assert(s1.end == None)  
    assert(s2.start == None)
    assert(s2.end == Some(Month(2020, 1)))
    assert(s3.start == Some(Month(1986, 1)))
    assert(s3.end == Some(Month(2020, 1)))
  }

  "SpecDate" can "be created from a Date" in {
    val q = Quarter(1986, 1)
    val m = Month(2020, 1)

    assert(SpecDate(q).value == q)
    assert(SpecDate(m).value == m)
  }

  it can "be created from a String" in {
    assert(SpecDate.fromString("1986.1").value == Quarter(1986, 1))
    assert(SpecDate.fromString("1986.1").value.frequency == 4)
    assert(SpecDate.fromString("1986.01").value == Month(1986, 1))
    assert(SpecDate.fromString("1986.01").value.frequency == 12)
    assert(SpecDate.fromString("1986.jan").value == Month(1986, 1))
    assert(SpecDate.fromString("1986.jan").value.frequency == 12)
    assert(SpecDate.fromString("1986.feb").value == Month(1986, 2))
    assert(SpecDate.fromString("1986.feb").value.frequency == 12)
    assert(SpecDate.fromString("1986.mar").value == Month(1986, 3))
    assert(SpecDate.fromString("1986.mar").value.frequency == 12)
    assert(SpecDate.fromString("1986.apr").value == Month(1986, 4))
    assert(SpecDate.fromString("1986.apr").value.frequency == 12)
    assert(SpecDate.fromString("1986.may").value == Month(1986, 5))
    assert(SpecDate.fromString("1986.may").value.frequency == 12)
    assert(SpecDate.fromString("1986.jun").value == Month(1986, 6))
    assert(SpecDate.fromString("1986.jun").value.frequency == 12)
    assert(SpecDate.fromString("1986.jul").value == Month(1986, 7))
    assert(SpecDate.fromString("1986.jul").value.frequency == 12)
    assert(SpecDate.fromString("1986.aug").value == Month(1986, 8))
    assert(SpecDate.fromString("1986.aug").value.frequency == 12)
    assert(SpecDate.fromString("1986.sep").value == Month(1986, 9))
    assert(SpecDate.fromString("1986.sep").value.frequency == 12)
    assert(SpecDate.fromString("1986.oct").value == Month(1986, 10))
    assert(SpecDate.fromString("1986.oct").value.frequency == 12)
    assert(SpecDate.fromString("1986.nov").value == Month(1986, 11))
    assert(SpecDate.fromString("1986.nov").value.frequency == 12)
    assert(SpecDate.fromString("1986.dec").value == Month(1986, 12))
    assert(SpecDate.fromString("1986.dec").value.frequency == 12)

    assertThrows[IllegalArgumentException]{SpecDate.fromString("1986.5")}
    assertThrows[IllegalArgumentException]{SpecDate.fromString("1986.13")}
    assertThrows[IllegalArgumentException]{SpecDate.fromString("1986.foo")}
  }

  it can "be created from JSON" in {
    assert(SpecDate.fromJSON("1986.01").value == Month(1986, 1))
    assert(SpecDate.fromJSON("\"1986.01\"").value == Month(1986, 1))
  }
  
  "SpecARMA" can "be created from List[Coefficient]" in {
    val c1 = NumericCoefficient(1, false)
    val c2 = NumericCoefficient(1, true)
    val c3 = NumericCoefficient.fromString("2")
    val c4 = NumericCoefficient.fromString("2f")
    val c5 = NumericCoefficient.fromString("3.0")
    val c6 = NumericCoefficient.fromString("3.0f")
    val s = SpecARMA(List(c1, c2, c3, c4, c5, c6))

    assert(s.value.map(_.asInstanceOf[NumericCoefficient].fixed) == List(false, true, false, true, false, true))
    assert(s.value.map(_.asInstanceOf[NumericCoefficient].value) == List(1.0, 1.0, 2.0, 2.0, 3.0, 3.0))
  }

  it can "be created from a String" in {
    val c1 = NumericCoefficient(1, false)
    val c2 = NumericCoefficient(1, true)
    val c3 = NumericCoefficient.fromString("2")
    val c4 = NumericCoefficient.fromString("2f")
    val c5 = NumericCoefficient.fromString("3.0")
    val c6 = NumericCoefficient.fromString("3.0f")

    val s1 = SpecARMA.fromString("(1 1f 2.0 2.0f 3 3.0f)")
    val s2 = SpecARMA.fromString("(1, 1f, 2.0, 2.0f, 3, 3.0f)")
    val s3 = SpecARMA.fromString("(1,, 1f, 2.0, 2.0f, 3, 3.0f)")

    assert(s1.value.map(_.asInstanceOf[NumericCoefficient].fixed) == List(false, true, false, true, false, true))
    assert(s1.value.map(_.asInstanceOf[NumericCoefficient].value) == List(1.0, 1.0, 2.0, 2.0, 3.0, 3.0))

    assert(s2.value.map(_.asInstanceOf[NumericCoefficient].fixed) == List(false, true, false, true, false, true))
    assert(s2.value.map(_.asInstanceOf[NumericCoefficient].value) == List(1.0, 1.0, 2.0, 2.0, 3.0, 3.0))

    assert(s3.value(1) == MissingCoefficient)
  }

  it can "be created from JSON" in {
    assert(true)
  }

  "SpecARIMA" can "be created from Strings and Models" in {
    val m1  = SpecARIMA.fromString("(0,1,1)")
    val m2  = SpecARIMA.fromString("(0 1 1)")
    val m3  = SpecARIMA.fromString("(0 1 1)12")
    val m4  = SpecARIMA.fromString("([2] 1 0)")
    val m5  = SpecARIMA.fromString("(2 1 0)(0 1 1)")
    val m6  = SpecARIMA.fromString("(0,1,1)(0,1,1)")
    val m7  = SpecARIMA.fromString("(0,1,1)(0,1,1)12")
    val m8  = SpecARIMA.fromString("(0 1 [1,3])")
    val m9  = SpecARIMA.fromString("([2 3] 1 1)(0 1 1)12")
    val m10 = SpecARIMA.fromString("([2 3] 0 0)")
    val m11 = SpecARIMA.fromString("(0 1 1)1(0 1 1)12")
    val m12 = SpecARIMA.fromString("(1 1 0)(1 0 0)3(0 0 1)")
    
    assert(m1 == SpecARIMA(List(Model(0, 1, 1))))
    assert(m2 == SpecARIMA(List(Model(0, 1, 1))))
    assert(m3 == SpecARIMA(List(Model(0, 1, 1, 12))))
    assert(m4 == SpecARIMA(List(Model(2, 1, 0))))
    assert(m5 == SpecARIMA(List(Model(2, 1, 0), Model(0, 1, 1))))
    assert(m6 == SpecARIMA(List(Model(0, 1, 1), Model(0, 1, 1))))
    assert(m7 == SpecARIMA(List(Model(0, 1, 1), Model(0, 1, 1, 12))))
    assert(m8 == SpecARIMA(List(Model(Order(0), 1, Order(List(1,3))))))
    assert(m9 == SpecARIMA(List(
      Model(Order(List(2,3)), 1, Order(1)),
      Model(0, 1, 1, 12)
    )))
    assert(m10 == SpecARIMA(List(Model(Order(List(2,3)), 0, Order(0)))))
    assert(m11 == SpecARIMA(List(
      Model(0, 1, 1, 1),
      Model(0, 1, 1, 12)
    )))
    assert(m12 == SpecARIMA(List(
      Model(1, 1, 0),
      Model(1, 0, 0, 3), 
      Model(0, 0, 1)
    )))
  }

  it can "be created from JSON and Models" in {
    val m1  = SpecARIMA.fromJSON("\"(0,1,1)\"")
    val m2  = SpecARIMA.fromJSON("\"(0 1 1)\"")
    val m3  = SpecARIMA.fromJSON("\"(0 1 1)12\"")
    val m4  = SpecARIMA.fromJSON("\"([2] 1 0)\"")
    val m5  = SpecARIMA.fromJSON("\"(2 1 0)(0 1 1)\"")
    val m6  = SpecARIMA.fromJSON("\"(0,1,1)(0,1,1)\"")
    val m7  = SpecARIMA.fromJSON("\"(0,1,1)(0,1,1)12\"")
    val m8  = SpecARIMA.fromJSON("\"(0 1 [1,3])\"")
    val m9  = SpecARIMA.fromJSON("\"([2 3] 1 1)(0 1 1)12\"")
    val m10 = SpecARIMA.fromJSON("\"([2 3] 0 0)\"")
    val m11 = SpecARIMA.fromJSON("\"(0 1 1)1(0 1 1)12\"")
    val m12 = SpecARIMA.fromJSON("\"(1 1 0)(1 0 0)3(0 0 1)\"")
    
    assert(m1 == SpecARIMA(List(Model(0, 1, 1))))
    assert(m2 == SpecARIMA(List(Model(0, 1, 1))))
    assert(m3 == SpecARIMA(List(Model(0, 1, 1, 12))))
    assert(m4 == SpecARIMA(List(Model(2, 1, 0))))
    assert(m5 == SpecARIMA(List(Model(2, 1, 0), Model(0, 1, 1))))
    assert(m6 == SpecARIMA(List(Model(0, 1, 1), Model(0, 1, 1))))
    assert(m7 == SpecARIMA(List(Model(0, 1, 1), Model(0, 1, 1, 12))))
    assert(m8 == SpecARIMA(List(Model(Order(0), 1, Order(List(1,3))))))
    assert(m9 == SpecARIMA(List(
      Model(Order(List(2,3)), 1, Order(1)),
      Model(0, 1, 1, 12)
    )))
    assert(m10 == SpecARIMA(List(Model(Order(List(2,3)), 0, Order(0)))))
    assert(m11 == SpecARIMA(List(
      Model(0, 1, 1, 1),
      Model(0, 1, 1, 12)
    )))
    assert(m12 == SpecARIMA(List(
      Model(1, 1, 0),
      Model(1, 0, 0, 3), 
      Model(0, 0, 1)
    )))
  }
}

// regression variables - AO2001.3 LSS2007.1-2007.3 AO2008.4