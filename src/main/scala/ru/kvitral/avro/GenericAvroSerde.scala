package ru.kvitral.avro

import java.util.Collections

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}

class GenericAvroSerde(serde: Serde[GenericRecord]) extends Serde[GenericRecord] {

  def this(client: SchemaRegistryClient, props: java.util.Map[String, _]) =
    this(Serdes.serdeFrom(new GenericAvroSerializer(client), new GenericAvroDeserializer(client, props)))

  def this(client: SchemaRegistryClient) = this(client, Collections.emptyMap[String, AnyRef])

  def this() = this(Serdes.serdeFrom(new GenericAvroSerializer, new GenericAvroDeserializer))

  def serializer: Serializer[GenericRecord] = serde.serializer

  def deserializer: Deserializer[GenericRecord] = serde.deserializer

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    serde.serializer.configure(configs, isKey)
    serde.deserializer.configure(configs, isKey)
  }

  def close(): Unit = {
    serde.serializer.close()
    serde.deserializer.close()
  }
}