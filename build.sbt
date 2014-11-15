lazy val root = project in file(".") aggregate(core, casbah)

lazy val core = project in file("core")

lazy val casbah = project in file("casbah") dependsOn(core)
