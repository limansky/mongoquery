name := "mongoquery"

version := "0.1"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"  %% "scala-parser-combinators"   % "1.0.2",
  "org.mongodb"             %% "casbah-core"                % "2.7.3",
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
)
