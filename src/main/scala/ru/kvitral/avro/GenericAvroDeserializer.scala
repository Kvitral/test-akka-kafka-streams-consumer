package ru.kvitral.avro

import java.util.Collections

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Deserializer

class GenericAvroDeserializer(client: SchemaRegistryClient, props: java.util.Map[String, _]) extends Deserializer[GenericRecord] {
  val kafkaAvroDeserializer: KafkaAvroDeserializer =
    Option(client).map(client => new KafkaAvroDeserializer(client, props)).getOrElse(new KafkaAvroDeserializer)

  def this(client: SchemaRegistryClient) = this(client, Collections.emptyMap[String, AnyRef])

  def this() = this(null, Collections.emptyMap[String, AnyRef])

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    kafkaAvroDeserializer.configure(configs, isKey)
  }

  def deserialize(s: String, bytes: Array[Byte]): GenericRecord = {
    kafkaAvroDeserializer.deserialize(s, bytes).asInstanceOf[GenericRecord]
  }

  def close(): Unit = {
    kafkaAvroDeserializer.close()
  }
}