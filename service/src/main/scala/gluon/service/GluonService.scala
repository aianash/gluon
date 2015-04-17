package gluon.service

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.concurrent._, duration._

import java.net.InetSocketAddress

import scalaz._, Scalaz._
import scalaz.std.option._
import scalaz.syntax.monad._

import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import akka.actor.{Actor, Props}
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.event.Logging

import com.goshoplane.common._
import com.goshoplane.gluon.service._
import gluon.catalogue._

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.finagle.Thrift
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod


import com.typesafe.config.{Config, ConfigFactory}

import org.apache.thrift.protocol.TBinaryProtocol

class GluonService(implicit inj: Injector) extends Gluon.FutureIface  {

  implicit val system = inject [ActorSystem]

  val log = Logging.getLogger(system, this)

  import system._

  val settings = GluonSettings(system)

  val Processor = system.actorOf(CatalogueProcessor.props)

  def publish(serializedCatalogueItem: SerializedCatalogueItem) = {
    Processor ! ProcessCatalogue(serializedCatalogueItem)
    TwitterFuture.value(true)
  }

}


object GluonService {

  def start(implicit inj: Injector) = {
    val settings = GluonSettings(inject [ActorSystem])

    val protocol = new TBinaryProtocol.Factory()
    val service  = new Gluon$FinagleService(inject [GluonService], protocol)
    val address  = new InetSocketAddress(settings.GluonHost, settings.GluonPort)

    ServerBuilder()
      .codec(ThriftServerFramedCodec())
      .name(settings.ServiceName)
      .bindTo(address)
      .build(service)
  }
}