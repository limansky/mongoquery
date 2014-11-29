import sbt._
import sbt.Keys._

object Common {
  val settings = Seq(
    version := "0.3-SNAPSHOT",
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4", "2.10.4"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    libraryDependencies <++= scalaVersion { sv =>
      CrossVersion.partialVersion(sv) match {
        case Some((2, 11)) => Seq()
        case Some((2, 10)) => Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
          "org.scalamacros" %% "quasiquotes" % "2.0.1"
        )
        case _ => sys.error("Unsupported Scala version")
      }
    },
    libraryDependencies ++= Seq(
      "org.scala-lang"  %  "scala-reflect"  % scalaVersion.value,
      "org.scalatest"   %% "scalatest"      % "2.2.2"             % "test"
    )
  )
}

object Publish {
  val settings = Seq(
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
    pomExtra := (
      <developers>
        <developer>
          <id>limansky</id>
          <name>Mike Limansky</name>
          <url>http://github.com/limansky</url>
        </developer>
      </developers>
    )
  )
}
