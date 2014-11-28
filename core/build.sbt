name := "mongoquery-core"

Common.settings

libraryDependencies <++= scalaVersion { sv =>
  CrossVersion.partialVersion(sv) match {
    case Some((2, v)) if v >= 11 => Seq(
      "org.scala-lang.modules"  %% "scala-parser-combinators"   % "1.0.2"
    )
    case _ => Nil
  }
}

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.0" % "test"

unmanagedSourceDirectories in Compile <+= (scalaBinaryVersion, sourceDirectory in Compile) { (v, d) =>
  d / s"scala_$v"
}

Publish.settings

scalariformSettings
