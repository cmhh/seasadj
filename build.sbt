name := """seasadj"""
organization := "org.cmhh"
scalaVersion := "2.13.4"
version := "0.2.0"
scalacOptions += "-deprecation"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.4"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.4" % Test
libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.1.1" % "test"

assemblyJarName in assembly := "seasadj.jar"

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}