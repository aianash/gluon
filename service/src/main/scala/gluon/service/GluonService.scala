package gluon.service

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.concurrent._, duration._
import scala.util.control.NonFatal

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

import org.apache.thrift.protocol.TBinaryProtocol

import goshoplane.commons.core.protocols.Implicits._



class GluonService(implicit inj: Injector) extends Gluon.FutureIface {

  implicit val system = inject [ActorSystem]

  import system._

  val log      = Logging.getLogger(system, this)
  val settings = GluonSettings(system)

  val Processor = system.actorOf(CatalogueProcessor.props)


  def publish(serializedCatalogueItem: SerializedCatalogueItem) = {
    implicit val timeout = Timeout(500 milliseconds)

    val successF = Processor ?= ProcessCatalogue(serializedCatalogueItem)

    awaitResult(successF, 500 milliseconds, {
      case NonFatal(ex) => TFailure(GluonException("Error while creating user"))
    })
  }




  /**
   * A helper method to await on Scala Future and encapsulate the result into TwitterFuture
   */
  private def awaitResult[T, U >: T](future: Future[T], timeout: Duration, ex: PartialFunction[Throwable, Try[U]]): TwitterFuture[U] = {
    TwitterFuture.value(Try {
      Await.result(future, timeout)
    } recoverWith(ex) get)
  }

}


object GluonService {

  def start(implicit inj: Injector) = {
    val settings = GluonSettings(inject [ActorSystem])

    val protocol = new TBinaryProtocol.Factory()
    val service  = new Gluon$FinagleService(inject [GluonService], protocol)
    val address  = new InetSocketAddress(settings.GluonHost, settings.GluonPort)

    ServerBuilder()
      .codec(ThriftServerFramedCodec(protocol))
      .name(settings.ServiceName)
      .bindTo(address)
      .build(service)
  }
}