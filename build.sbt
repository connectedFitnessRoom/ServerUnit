ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

// Adding Slick to project

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.5.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2", // HikariCP for connection pooling
  "org.slf4j" % "slf4j-nop" % "2.0.16",
  "com.h2database" % "h2" % "2.3.232"
)


// Add Akka artifacts to project
resolvers += "Akka library repository".at("https://repo.akka.io/maven")
// Adding Akka, Akka MQTT, and Akka Streams to project
val AkkaVersion = "2.10.0"
val AkkaHttpVersion = "10.7.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "9.0.0",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

)

val playVersion = "3.0.4"
libraryDependencies += "org.playframework" %% "play-json" % playVersion

lazy val root = (project in file("."))
  .settings(
    name := "ServerUnit",
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.6.0"
  )
