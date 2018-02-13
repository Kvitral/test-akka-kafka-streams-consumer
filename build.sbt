
name := "test-akka-kafka-streams-consumer"

version := "0.1"

scalaVersion := "2.12.4"


val kafka_streams_scala_version = "0.1.2"

libraryDependencies ++= Seq("com.lightbend" %%
  "kafka-streams-scala" % kafka_streams_scala_version)

libraryDependencies += "com.typesafe" % "config" % "1.3.2"


lazy val main = (project in file(".")).settings(
  assemblyJarName := "kafka-streams-example.jar",
  mainClass := Some("ru.kvitral.KafkaStreamsExample")
)
