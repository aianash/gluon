package gluon.catalogue

import scala.util.{Failure, Success, Try}
import scala.concurrent._, duration._
import scala.util.control.NonFatal

import scalaz._, Scalaz._
import scalaz.std.option._
import scalaz.syntax.monad._

import akka.actor.{Actor, Props, ActorLogging, ActorSystem}
import akka.util.Timeout
import akka.pattern.pipe

import com.goshoplane.common._

import gluon.catalogue._
import gluon.catalogue.validators._
import gluon.catalogue.publishers._

import goshoplane.commons.core.protocols.Implicits._


/**
 * This `Actor` performs the associated processing of a catalogue
 * item.
 * Currently processing involves
 * - Validation/Transforming catalogue item
 * - Publishing item to kafka if valid
 *
 * [NOTE] Currently its not very Reactive design, because
 * of ask request to Validator, but could easily be converted into.
 */
class CatalogueProcessor extends Actor with ActorLogging {

  import context.dispatcher

  val Validator = context.actorOf(CatalogueValidator.props)
  val Publisher = context.actorOf(CataloguePublisher.props)

  context watch Validator
  context watch Publisher

  def receive = {

    case ProcessCatalogue(item) =>
      implicit val timeout = Timeout(2 seconds)

      (Validator ?= ValidateCatalogue(item))    // 1. Validate catalogue item
        .recover {
          case NonFatal(ex) =>
            log.error(ex, "Caught error = {} from validator for item id = {}.{}",
                          ex.getMessage,
                          item.itemId.storeId.stuid,
                          item.itemId.cuid)

            false // 2. After logging error mark it as not valid
                  // Exception is not propagated further
        } andThen {
          case Success(valid) if valid =>
            Publisher ! PublishCatalogue(item) // 3. Send to Publisher as side effect
                                               // because immediately after validation
                                               // tell the sender that processing is finished
                                               // and let Gluon take the responsibility
                                               // to do so
        } pipeTo sender()

  }

}


object CatalogueProcessor  {
  def props = Props[CatalogueProcessor]
}