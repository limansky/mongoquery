import ReleaseTransformations._

lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

lazy val root = project in file(".") aggregate (core, casbah, reactivemongo, scala_driver) settings (
  commonSettings,
  publishSettings,
  // According to the docs - crossScalaVersions must be set to Nil on the aggregating project to avoid double publishing
  crossScalaVersions := Nil,
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val core = (project in file("core"))
  .settings(
    name := "mongoquery-core",
    commonSettings,
    publishSettings,
    releaseSettings,
    crossScalaVersions := Seq(scala212, scala211),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test
    )
  )

lazy val casbah = (project in file("casbah"))
  .dependsOn(core % "test->test ; compile->compile")
  .settings(
    name := "mongoquery-casbah",
    commonSettings,
    crossScalaVersions := Seq(scala212, scala211),
    publishSettings,
    releaseSettings,
    libraryDependencies += "org.mongodb" %% "casbah-core" % "3.1.1" % Provided
  )

lazy val scala_driver = (project in file("scala_driver"))
  .dependsOn(core % "test->test ; compile->compile")
  .settings(
    name := "mongoquery-scala-driver",
    commonSettings,
    crossScalaVersions := Seq(scala212, scala211),
    publishSettings,
    releaseSettings,
    libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0" % Provided
  )

lazy val reactivemongo = (project in file("reactivemongo"))
  .dependsOn(core % "test->test ; compile->compile")
  .settings(
    name := "mongoquery-reactive",
    commonSettings,
    crossScalaVersions := Seq(scala212, scala211),
    publishSettings,
    releaseSettings,
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.12.6" % Provided
  )

lazy val commonSettings = Seq(
  scalaVersion := scala212,
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "3.0.8" % "test"
  )
)

lazy val publishSettings = Seq(
  licenses += ("Apache 2.0 License", url(
    "http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("http://github.com/limansky/mongoquery")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
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
  developers := List(
    Developer("limansky", "Mike Limansky", "mike.limansky@gmail.com", url("http://github.com/limansky"))
  )
)

lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseTagName := s"mongoquery_${version.value}",
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)
