package ru.kvitral

import java.util.Properties

import com.typesafe.config.Config
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig

class AppConfig(config: Config) {


  val bootstrapServers = config.getString("kafka.bootstrap-servers")
  val streamId = config.getString("kafka.stream-id")
  val streamingConfig = {
    val settings = new Properties
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer.getClass.getName)
    settings
  }

  val upstream = config.getString("kafka.upstream")
  val downstream = config.getString("kafka.downstream")

  val reducingKey = config.getString("kafka.reducing-key")
  val keyedIntsTopic = config.getString("kafka.keyed-ints-topic")

}
