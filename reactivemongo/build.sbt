name := "mongoquery-reactive"

Common.settings

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.11.6" % "provided"

Publish.settings

scalariformSettings
