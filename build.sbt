lazy val root = project in file(".") aggregate(core, casbah, reactivemongo, compiletest) settings( publish := {}, publishLocal := {} )

lazy val core = project in file("core") disablePlugins(CoverallsPlugin)

lazy val casbah = project in file("casbah") dependsOn(core) disablePlugins(CoverallsPlugin)

lazy val reactivemongo = project in file ("reactivemongo") dependsOn(core) disablePlugins(CoverallsPlugin)

lazy val compiletest = project in file ("compiletest") dependsOn (casbah) disablePlugins(CoverallsPlugin)
