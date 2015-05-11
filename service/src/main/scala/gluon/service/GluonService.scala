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


/**
 * Gluon Service's primary purpose is to inject catalogue items into
 * Shoplane's internal backend services' through kafka.
 * For that it provides `publish` api
 *
 * Service class that implements the Thrift's auto generated apis in
 * [[com.goshoplane.gluon.service.Gluon.FutureIface]]. The thrift files
 * are present in commons-core package.
 *
 * This service uses actor [[gluon.catalogue.CatalogueProcessor]]
 * for its publish api
 *
 */
class GluonService(implicit inj: Injector) extends Gluon.FutureIface {

  implicit val system = inject [ActorSystem]

  import system._

  val log      = Logging.getLogger(system, this)
  val settings = GluonSettings(system)

  // Catalogue Processor Actor that does the heavy uplifting
  val Processor = system.actorOf(CatalogueProcessor.props)


  /**
   * Publish catalogue item to internal systems
   *
   * @param item  Serialized catalogue item
   * @return      [[com.twitter.util.Future]] instance of the result (i.e. whether successfuly published)
   */
  def publish(item: SerializedCatalogueItem) = {
    implicit val timeout = Timeout(500 milliseconds) // [TO IMPROVE] timeout management

    val successF = Processor ?= ProcessCatalogue(item)

    awaitResult(successF, 500 milliseconds, {
      case NonFatal(ex) =>
        val statement = s"Error while publishing catalogue item" +
                        s" for itemId = ${item.itemId.storeId.stuid}.${item.itemId.cuid}"

        log.error(ex, statement)
        TFailure(GluonException(statement))
    })
  }



  /**
   * A helper method to await on Scala Future and encapsulate the result into TwitterFuture
   * This is done mainly because all the api methods from thrift generated code returns
   * [[com.twitter.util.Future]]
   *
   * @tparam T  Type of the element the `Future` will resolve into
   * @tparam U  Since T is covariant type, an upper bound on T or using U
   *            as supertype allows using in parameter
   * @param  future   [[scala.concurrent.Future]] to await for result
   * @param  timeout  max await time
   * @param  ex       How to handle exception in case of failure
   * @return          result of the future encapsulated inside Twitter future
   */
  private def awaitResult[T, U >: T](future: Future[T], timeout: Duration, ex: PartialFunction[Throwable, Try[U]]): TwitterFuture[U] = {
    TwitterFuture.value(Try {
      Await.result(future, timeout)
    } recoverWith(ex) get)
  }

}


/**
 * Gluon Service companion object
 */
object GluonService {

  /**
   * Starts the Gluon Service. and injector is passed
   * implicitly, which should be able to inject [[akka.actor.ActorSystem]]
   * and [[gluon.service.GluonService]]
   *
   * @param inj  the injector for injecting ActorSystem and GluonService instance
   * @return     [[com.twitter.finagle.builder.Server]] instance representing the GluonService
   */
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