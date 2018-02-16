package ru.kvitral

import java.util.Collections
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer, KafkaAvroSerializer}
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.kafka.common.serialization.{Serde, Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.internals.{WindowedDeserializer, WindowedSerializer}
import org.apache.kafka.streams.kstream.{TimeWindows, _}
import org.apache.kafka.streams.state.WindowStore
import ru.kvitral.avro.GenericAvroSerde

import scala.io.Source


object KafkaStreamsExample extends App {


  //default typesafe config initialization
  val config = ConfigFactory.load

  initStream(config).start()

  def initStream(config: Config): KafkaStreams = {

    val appConfig = new AppConfig(config)

    val builder = new StreamsBuilder
    val stringSerdes: Serde[String] = Serdes.String()
    val integerSerdes: Serde[Integer] = Serdes.Integer()
    val stringSerializer = new StringSerializer
    val stringDeserializer = new StringDeserializer

    val windowedSerde = Serdes.serdeFrom(new WindowedSerializer(stringSerializer), new WindowedDeserializer(stringDeserializer))

    val avroSerde = new GenericAvroSerde()
    val isKeySerde = false
    avroSerde.configure(
      Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, appConfig.schemaRegistryServer),
      isKeySerde)

    val downstreamProcuder = Produced.`with`(windowedSerde, avroSerde)
    val throughProducer = Produced.`with`(stringSerdes, integerSerdes)

    val schema: Schema = new Parser().parse(Source.fromURL(getClass.getResource("/countedInt.avsc")).mkString)
    val genericRecordBuilder = new GenericRecordBuilder(schema)

    val windowSizeMs = TimeUnit.SECONDS.toMillis(appConfig.windowRangeSec)

    /*
    * advanceBy here for study purpose
    * in general advanceBy() will make hopping window but if we make
    * windowSize == advanceBySize then again we will come to a tumbling window
    * (see JavaDoc for semantic of method of() )
     */

    val window = TimeWindows.of(windowSizeMs).advanceBy(windowSizeMs)

    TimeWindows.of(windowSizeMs)
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


    /*
    * Zero element for aggregate
   */
    val zeroEl: GenericRecord = genericRecordBuilder.build()

    /*
    *
    * Custom aggregator using explicit apply.
    * Need to figure out why
    *
     */
    val aggregator = new Aggregator[String, Integer, GenericRecord] {
      override def apply(key: String, value: Integer, aggregate: GenericRecord): GenericRecord =
        genericRecordBuilder.set("count", Integer.valueOf(aggregate.get("count").toString) + value).build()
    }

    /*
    * Explicit materialized coz scala go crazy about type classes
    * Need to figure out why
    *
     */

    val materialized: Materialized[String, GenericRecord, WindowStore[Bytes, Array[Byte]]] =
      Materialized.as("StoredCounts").withKeySerde(stringSerdes).withValueSerde(avroSerde)



    /*
    * store keyed topic for other possible aggregations is future via through() command
    * then we make this stream to a windowed one and finally reduce the result
    *
     */

    val reduceStream = enrichWithKey
      .through(appConfig.keyedIntsTopic, throughProducer)
      .groupByKey().windowedBy(window).aggregate(
      () => zeroEl, aggregator, materialized)


    //send aggregated value to downstream

    reduceStream.toStream.to(appConfig.downstream, downstreamProcuder)

    new KafkaStreams(builder.build(), appConfig.streamingConfig)


  }
}
