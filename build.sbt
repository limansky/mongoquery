name := "mongoquery"

version := "0.2"

scalaVersion := "2.11.4"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scala-lang.modules"  %% "scala-parser-combinators"   % "1.0.2",
  "org.mongodb"             %% "casbah-core"                % "2.7.4",
  "org.scala-lang"          % "scala-reflect"               % scalaVersion.value,
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
)

scalariformSettings
