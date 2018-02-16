package ru.kvitral.avro

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

class GenericAvroSerializer(client: SchemaRegistryClient) extends Serializer[GenericRecord] {
  val kafkaAvroSerializer: KafkaAvroSerializer =
    Option(client).map(client => new KafkaAvroSerializer(client)).getOrElse(new KafkaAvroSerializer)

  def this() = this(null)

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    kafkaAvroSerializer.configure(configs, isKey)
  }

  def serialize(topic: String, record: GenericRecord): Array[Byte] = {
    kafkaAvroSerializer.serialize(topic, record)
  }

  def close(): Unit = {
    kafkaAvroSerializer.close()
  }
}