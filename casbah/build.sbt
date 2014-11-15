name := "mongoquery-casbah"

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions := Common.scalacOptions

libraryDependencies ++= Seq(
  "org.mongodb"             %% "casbah-core"                % "2.7.4"       % "provided",
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
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
