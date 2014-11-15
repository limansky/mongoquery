name := "mongoquery-core"

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions := Common.scalacOptions

libraryDependencies ++= Seq(
  "org.scala-lang"          % "scala-reflect"               % scalaVersion.value,
  "org.scalatest"           %% "scalatest"                  % "2.2.1"       % "test"
)

libraryDependencies <++= scalaVersion { sv =>
  CrossVersion.partialVersion(sv) match {
    case Some((2, 11)) => Seq(
      "org.scala-lang.modules"  %% "scala-parser-combinators"   % "1.0.2"
    )
    case Some((2, 10)) => Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
      "org.scalamacros" %% "quasiquotes" % "2.0.1"
    )
    case _ => error("Unsupported Scala version")
  }
}

unmanagedSourceDirectories in Compile <+= (scalaBinaryVersion, sourceDirectory in Compile) { (v, d) =>
  d / s"scala_$v"
}

scalariformSettings
