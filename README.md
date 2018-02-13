# test-akka-kafka-streams-consumer

Simple example ofr kafka-streams on scala using Stream-DSL
Picks up ints as string from upstream then enrich them with one specific key and maps values to Integer
Then pass mapped key-value pair into specific topic and then finally perform groupByKey and then reduce entries to perform sum

## Install Kafka
You need to setup and start Kafka [using quickstart guide]("https://kafka.apache.org/quickstart")
Don\`t forget to add `advertised.host.name= %your_vm_ip%` to `../config/server-properties` inside your virtual machine if you will run kafka from it 

## How to build it

Simply run sbt with command
```
sbt assembly
```
You will find yor shiny new jar inside `../target/scala-2.12/kafka-streams-example.jar`

## How to run it

You can run compiled jar like any other jar
`java -Dkafka.bootstrap-servers=localhost:9092 -jar kafka-streams-example.jar`

Full list of posibile parameters can be found in `../main/resources/application.conf` in `kafka` section
