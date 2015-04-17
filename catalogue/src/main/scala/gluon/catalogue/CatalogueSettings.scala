package gluon.catalogue

import akka.actor.{ActorSystem, Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem}

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Gluon specific settings
 */
class CatalogueSettings(cfg: Config) extends Extension {

  // validate gluon config
  final val config: Config = {
    val config = cfg.withFallback(ConfigFactory.defaultReference)
    config.checkValid(ConfigFactory.defaultReference, "gluon")
    config
  }

  val KafkaPort     = config.getInt("gluon.catalogue.kafka.port")
  val KafkaHost     = config.getString("gluon.catalogue.kafka.host")
  val KafkaEndpoint = KafkaHost + ":" + KafkaPort
  val KafkaClientId = config.getString("gluon.catalogue.kafka.client-id")
  val KafkaTopic    = config.getString("gluon.catalogue.kafka.topic")

  val KafkaKeySerializer        = config.getString("gluon.catalogue.kafka.serializer.key")
  val KafkaValueSerializer      = config.getString("gluon.catalogue.kafka.serializer.value")
  val KafkaPartitionerClass     = config.getString("gluon.catalogue.kafka.partitioner-class")
  val KafkaRequestsRequiredAcks = config.getString("gluon.catalogue.kafka.request-required-acks")
}


object CatalogueSettings extends ExtensionId[CatalogueSettings] with ExtensionIdProvider {
  override def lookup = CatalogueSettings

  override def createExtension(system: ExtendedActorSystem) =
    new CatalogueSettings(system.settings.config)

  override def get(system: ActorSystem): CatalogueSettings = super.get(system);
}