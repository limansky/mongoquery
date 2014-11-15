name := "mongoquery-casbah"

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions := Common.scalacOptions

libraryDependencies ++= Seq(
  "org.mongodb"             %% "casbah-core"                % "2.7.4"       % "provided",
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
)
