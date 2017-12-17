lazy val root = project in file(".") aggregate(core, casbah, reactivemongo) settings(
  commonSettings,
  publish := {},
  publishLocal := {}
)

lazy val core = project in file("core") settings (commonSettings, publishSettings) disablePlugins CoverallsPlugin

lazy val casbah = (project in file("casbah"))
  .dependsOn(core % "test->test ; compile->compile")
  .settings(commonSettings, publishSettings)
  .disablePlugins(CoverallsPlugin)

lazy val reactivemongo = (project in file ("reactivemongo"))
  .dependsOn(core % "test->test ; compile->compile")
  .settings(commonSettings, publishSettings)
  .disablePlugins(CoverallsPlugin)

lazy val commonSettings = Seq(
  version := "0.6-SNAPSHOT",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.12.4", "2.11.12", "2.10.7"),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.1.0"
      )
      case Some((2, x)) if x >= 11 => Seq()
      case _ => sys.error("Unsupported Scala version")
    }
  },
  libraryDependencies ++= Seq(
    "org.scala-lang"  %  "scala-reflect"  % scalaVersion.value,
    "org.scalatest"   %% "scalatest"      % "3.0.4"             % "test"
  ),
  unmanagedSourceDirectories in Compile ++= {
    (unmanagedSourceDirectories in Compile).value
      .filter(_.getName == "scala")
      .flatMap(f => CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => Seq()
        case Some((2, x)) if x >= 11 => Seq(new File(f.getPath + "-2.11+"))
      })
    }
)

lazy val publishSettings = Seq(
  licenses += ("Apache 2.0 License", url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("http://github.com/limansky/mongoquery")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  organization := "com.github.limansky",
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/limansky/mongoquery"),
      "scm:git:https://github.com/limansky/mongoquery.git",
      Some("scm:git:git@github.com:limansky/mongoquery.git")
    )
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := <developers>
    <developer>
      <id>limansky</id>
      <name>Mike Limansky</name>
      <url>http://github.com/limansky</url>
    </developer>
  </developers>
)
