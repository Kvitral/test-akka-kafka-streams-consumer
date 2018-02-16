
name := "test-akka-kafka-streams-consumer"

version := "0.1"

scalaVersion := "2.12.4"

resolvers +=  "confluent" at "http://packages.confluent.io/maven"


val kafka_streams_scala_version = "0.1.2"

libraryDependencies ++= Seq("com.lightbend" %%
  "kafka-streams-scala" % kafka_streams_scala_version)

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "org.apache.avro" % "avro" % "1.8.2"

libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "4.0.0"


lazy val main = (project in file(".")).settings(
  assemblyJarName := "kafka-streams-example.jar",
  mainClass := Some("ru.kvitral.KafkaStreamsExample")
)
