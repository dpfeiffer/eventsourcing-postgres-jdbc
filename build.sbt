lazy val root = project
  .in(file("."))
  .settings(
    name := "eventsourcing-jdbc-example",
    libraryDependencies ++= Seq(
      "org.postgresql"    % "postgresql" % "42.2.8",
      "com.typesafe.play" %% "play-json" % "2.7.4"
    )
  )
