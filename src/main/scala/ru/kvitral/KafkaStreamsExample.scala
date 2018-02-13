package ru.kvitral

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._


object KafkaStreamsExample extends App {


  //default typesafe config initialization
  val config = ConfigFactory.load

  initStream(config).start()

  def initStream(config: Config): KafkaStreams = {

    val appConfig = new AppConfig(config)

    val builder = new StreamsBuilder
    val stringSerdes: Serde[String] = Serdes.String()
    val integerSerdes: Serde[Integer] = Serdes.Integer()
    val downstreamProcuded = Produced.`with`(stringSerdes, integerSerdes)

    val upstream: KStream[String, String] = builder.stream(appConfig.upstream, Consumed.`with`(stringSerdes, stringSerdes))

    /*
    * for the study purpose only.
    * There is always a possibility to read upsream of ints as Integer :)
    *
    * For aggregate u first need to group values and provide some key if upstream doesn`t have one
    * You can go with upstream.selectKey[String]((k,v) => "r") be aware that u need explicitly pass result type
    * Or u can use KStream#map for that purpose
    * */
    val enrichWithKey: KStream[String, Integer] = upstream.map((_, v) => KeyValue.pair(appConfig.reducingKey, Integer.valueOf(v)))

    //store keyed topic for other possible aggregations is future
    val reduceStream = enrichWithKey.through(appConfig.keyedIntsTopic, downstreamProcuded).groupByKey().reduce((x, y) => x + y)

    //send aggregated value to downstream
    reduceStream.toStream.to(appConfig.downstream, downstreamProcuded)

    new KafkaStreams(builder.build(), appConfig.streamingConfig)
  }


}
