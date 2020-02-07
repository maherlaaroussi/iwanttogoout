import com.typesafe.config._

version := "0.1"
name := "iwanttogoout-scala-akka-http-server"
organization := "io.swagger"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-actor" % "2.5.26",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
)

fork in run := true
val conffactory = ConfigFactory.load()
