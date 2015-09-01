// For sbt-scoverage 1.3.0
resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.4.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")
