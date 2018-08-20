name := "DWT"

version := "0.1"
scalaVersion := "2.12.6"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing"     % "2.0.2",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
autoCompilerPlugins := true

fork := true