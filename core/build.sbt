name := "mongoquery-core"

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 11 => Seq(
      "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.0.6"
    )
    case _ => Nil
  }
}

libraryDependencies ++= Seq(
  "org.scalacheck"  %% "scalacheck"         % "1.13.4"              % "test",
  "org.scala-lang"  %  "scala-compiler"     % scalaVersion.value    % "test"
)
