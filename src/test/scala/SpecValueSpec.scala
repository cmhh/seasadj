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

  /*
  "SpecSpan" can "be created from Dates" in {
    val q1 = Quarter(1986, 1)
    val q2 = Quarter(1996, 4)
    val m1 = Month(2020, 1)
    val m2 = Month(2020, 6)
    val s1 = SpecSpan(Some(q1), Some(q2))
    val s2 = SpecSpan(Some(m1), Some(m2))
  }
  */
}