package gluon.service

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem}

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Gluon specific settings
 */
class GluonSettings(cfg: Config) extends Extension {

  // validate gluon config
  final val config: Config = {
    val config = cfg.withFallback(ConfigFactory.defaultReference)
    config.checkValid(ConfigFactory.defaultReference, "gluon")
    config
  }

  val ActorSystem   = config.getString("gluon.actorSystem")
  val ServiceId     = config.getLong("gluon.service.id")
  val DatacenterId  = config.getLong("gluon.datacenter.id")

  val GluonEndpoint = config.getString("gluon.endpoint") + ":" + config.getInt("gluon.port")
}


object GluonSettings extends ExtensionId[GluonSettings] with ExtensionIdProvider {
  override def lookup = GluonSettings

  override def createExtension(system: ExtendedActorSystem) =
    new GluonSettings(system.settings.config)

  override def get(system: ActorSystem): GluonSettings = super.get(system);
}