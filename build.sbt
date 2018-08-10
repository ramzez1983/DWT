name := "DWT"

version := "0.1"
scalaVersion := "2.12.6"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing"     % "2.0.2",
  "junit"                   % "junit"           % "4.12" % "test",
  "com.novocode"            % "junit-interface" % "0.11" % "test"
)
autoCompilerPlugins := true

fork := true