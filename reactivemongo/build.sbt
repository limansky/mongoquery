name := "mongoquery-reactive"

Common.settings

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23" % "provided"

Publish.settings

scalariformSettings
