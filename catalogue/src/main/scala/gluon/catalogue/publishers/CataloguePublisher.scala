package gluon.catalogue.publishers

import java.util.Properties

import scala.util.control.NonFatal

import akka.actor.{Actor, Props, ActorSystem, ActorLogging}
import akka.util.Timeout

import com.goshoplane.common._

import gluon.catalogue.CatalogueSettings

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization._



class CataloguePublisher extends Actor  with ActorLogging {

  val props = new Properties

  val settings = CatalogueSettings(context.system)
  import settings._

  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      KafkaEndpoint)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaValueSerializer)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   KafkaKeySerializer)
  props.put(ProducerConfig.CLIENT_ID_CONFIG,              KafkaClientId)
  props.put(ProducerConfig.ACKS_CONFIG,                   KafkaRequestsRequiredAcks)


  val publisher = new KafkaProducer[String, SerializedCatalogueItem](props)

  def receive = {

    case PublishCatalogue(item) =>
      val key    = item.itemId.storeId.stuid.toString
      val record = new ProducerRecord(KafkaTopic, key, item)

      try {
        publisher.send(record).get()
      } catch {
        case NonFatal(ex) => log.error("Error while sending catalogue item", item, ex)
      }

  }

}

object CataloguePublisher {
  def props = Props[CataloguePublisher]
}