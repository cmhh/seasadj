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
    assert(SpecStringArray.fromString("(msr )").value.toList  == List("msr"))
    assert(SpecStringArray.fromString("( msr)").value.toList  == List("msr"))
    assert(SpecStringArray.fromString("( msr )").value.toList == List("msr"))
    assert(SpecStringArray.fromString("(a,b)").value.toList   == List("a", "b"))
    assert(SpecStringArray.fromString("(a b)").value.toList   == List("a", "b"))
    assert(SpecStringArray.fromString("(a,)").value.toList    == List("a", ""))
    assert(SpecStringArray.fromString("('a',)").value.toList  == List("a", ""))
  }
}