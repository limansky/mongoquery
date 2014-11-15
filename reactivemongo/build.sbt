name := "mongoquery-reactive"

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions := Common.scalacOptions

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.reactivemongo"       %% "reactivemongo"              % "0.10.5.0.akka23"     % "provided",
  "org.scalatest"           %% "scalatest"                  % "2.2.1"               % "test"
)

libraryDependencies <++= scalaVersion { sv =>
  CrossVersion.partialVersion(sv) match {
    case Some((2, 11)) => Seq()
    case Some((2, 10)) => Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
      "org.scalamacros" %% "quasiquotes" % "2.0.1"
    )
    case _ => error("Unsupported Scala version")
  }
}

scalariformSettings
