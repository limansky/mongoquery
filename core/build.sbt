name := "mongoquery-core"

Common.settings

libraryDependencies <++= scalaVersion { sv =>
  CrossVersion.partialVersion(sv) match {
    case Some((2, v)) if v >= 11 => Seq(
      "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.0.4"
    )
    case _ => Nil
  }
}

libraryDependencies ++= Seq(
  "org.scalacheck"  %% "scalacheck"         % "1.13.0"              % "test",
  "org.scala-lang"  %  "scala-compiler"     % scalaVersion.value    % "test"
)

Publish.settings
