package gluon.catalogue.validators

import scala.concurrent._, duration._
import scala.util.control.NonFatal

import scalaz._, Scalaz._
import scalaz.std.option._
import scalaz.syntax.monad._

import akka.actor.{Actor, Props, ActorLogging}
import akka.util.Timeout

import com.goshoplane.common._

import goshoplane.commons.catalogue._


/**
 * This `Actor` is used for validating serialized catalogue item
 * before publishing it to kafka, so as to prevent malformed
 * serialized items entering into the backend
 *
 * Currently it just performs deserialization check, but soon will
 * be performing more complicated checks on item attributes
 * and even updating with normalized values
 */
class CatalogueValidator extends Actor with ActorLogging {

  def receive = {

    // Validate catalogue item
    case ValidateCatalogue(item) =>
      try {
        CatalogueItem.decode(item) // blocking call here
        sender() ! true // if decode successful then
                        // send true to the sender()
      } catch {
        case NonFatal(ex) =>
          log.error(ex, "Caught error = {} while validating catalogue item id = {}.{}",
                        ex.getMessage,
                        item.itemId.storeId.stuid,
                        item.itemId.cuid)
          sender() ! false // if any error occured while decoding
                           // then send false
      }

  }

}


/**
 * Companion object to CatalogueValidator
 */
object CatalogueValidator {

  // [TO DO] Create a pool of validators
  def props = Props[CatalogueValidator]
}