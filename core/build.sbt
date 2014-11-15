name := "mongoquery-core"

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions := Common.scalacOptions

libraryDependencies ++= Seq(
  "org.scala-lang.modules"  %% "scala-parser-combinators"   % "1.0.2",
  "org.scala-lang"          % "scala-reflect"               % scalaVersion.value,
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
)

scalariformSettings
