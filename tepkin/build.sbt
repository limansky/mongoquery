name := "mongoquery-tepkin"

Common.settings

resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeRepo("snapshots")
)

libraryDependencies += "net.fehmicansaglam" %% "tepkin" % "0.5-SNAPSHOT"

Publish.settings

scalariformSettings
