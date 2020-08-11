package org.cmhh.seasadj

/**
 * Represent input type&ndash;string or JSON.
 */
sealed trait InputType
case object STRING extends InputType { override val toString = "String" }
case object JSON extends InputType { override val toString = "JSON" }