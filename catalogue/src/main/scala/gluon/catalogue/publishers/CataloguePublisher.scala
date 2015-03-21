package gluon.catalogue.publishers

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization._
import java.util.Properties

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.concurrent._, duration._

import scalaz._, Scalaz._
import scalaz.std.option._
import scalaz.syntax.monad._

import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import akka.actor.{Actor, Props}
import akka.actor.ActorSystem
import akka.actor.ActorLogging
import akka.util.Timeout
import akka.event.Logging

import com.goshoplane.common._

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.finagle.Thrift
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod

import com.typesafe.config.{Config, ConfigFactory}

class CataloguePublisher extends Actor  with ActorLogging{
  
  val props = new Properties
   
  props.put("metadata.broker.list", "broker:9092");
  props.put("serializer.class", "kafka.serializer.StringEncoder");

   props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
   props.put(ProducerConfig.CLIENT_ID_CONFIG, "my-producer");
  props.put("partitioner.class", "kafka.producer.DefaultPartitioner");
  props.put("request.required.acks", "1");

  val publisher = new KafkaProducer[String, String](props)

  def receive = {
    case PublishCatalogue(serializedCatalogueItem) =>
      println("Message received at publisher from processor")
      val a = publisher.send(new ProducerRecord[String, String]("test","key", "hello world!")).get()
      println("Message sent to kakfa ------------ result is "  + a.toString)
      sender() ! true
  }
}

object CataloguePublisher {
  def props = Props[CataloguePublisher]
}