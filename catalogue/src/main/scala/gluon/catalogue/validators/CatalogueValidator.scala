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

    case ValidateCatalogue(serializedCatalogueItem) =>
      try {
        CatalogueItem.decode(serializedCatalogueItem)
        sender() ! true
      } catch {
        case NonFatal(ex) =>
          log.warning("Invalid catalogue item received", serializedCatalogueItem.itemId, ex)
          sender() ! false
      }
  }

}

object CatalogueValidator {
  def props = Props[CatalogueValidator]
}