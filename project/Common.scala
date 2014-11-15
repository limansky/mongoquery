
object Common {
  val version = "0.2-SNAPSHOT"

  val scalaVersion = "2.11.4"

  val crossScalaVersions = Seq("2.10.4")

  def scalacOptions = Seq("-deprecation", "-unchecked", "-feature")
}
