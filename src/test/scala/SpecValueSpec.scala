package org.cmhh.seasadj

import org.scalatest._

class SpecValueSpec extends UnitSpec {  
  "SpecBool" can "be created from a Boolean" in {
    val t = SpecBool(true)
    val f = SpecBool(false)

    assert(t.value == true)
    assert(f.value == false)
  }

  "SpecBool" can "be created from a String" in {
    val t = SpecBool("true")
    val f = SpecBool("false")

    assert(t == SpecBool.YES)
    assert(f == SpecBool.NO)
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