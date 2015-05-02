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
 * Currently just verifies if the catalogue item
 * is decodable
 */
class CatalogueValidator extends Actor with ActorLogging {

  def receive = {

    case ValidateCatalogue(item) =>
      try {
        CatalogueItem.decode(item)
        sender() ! true // if decode successful then
                        // send true to the client of
                        // this actor
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

object CatalogueValidator {
  def props = Props[CatalogueValidator]
}