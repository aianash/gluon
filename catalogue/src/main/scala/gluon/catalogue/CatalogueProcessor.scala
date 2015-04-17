package gluon.catalogue

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.concurrent._, duration._

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


class CatalogueProcessor extends Actor with ActorLogging {

  import context.dispatcher

  val catalogueValidator = context.actorOf(CatalogueValidator.props)
  val cataloguePublisher = context.actorOf(CataloguePublisher.props)

  context watch catalogueValidator
  context watch cataloguePublisher


  def receive = {

    case ProcessCatalogue(serializedCatalogueItem) =>
      implicit val timeout = Timeout(2 seconds)
      val successF = catalogueValidator ?= ValidateCatalogue(serializedCatalogueItem)

      successF.filter(x => x).foreach { _ =>
        cataloguePublisher ! PublishCatalogue(serializedCatalogueItem)
      }

      successF pipeTo sender()
  }

}

object CatalogueProcessor  {
  def props = Props[CatalogueProcessor]
}