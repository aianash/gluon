package gluon.catalogue

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
import akka.pattern.ask
import akka.event.Logging


import com.goshoplane.common._
import gluon.catalogue._
import gluon.catalogue.validators._
import gluon.catalogue.publishers._

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.finagle.Thrift
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod

import com.typesafe.config.{Config, ConfigFactory}

class CatalogueProcessor extends Actor with ActorLogging {

    import context.dispatcher

    val catalogueValidator = context.actorOf(CatalogueValidator.props)
    
    val cataloguePublisher = context.actorOf(CataloguePublisher.props)

    def receive = {
      case ProcessCatalogue(serializedCatalogueItem) =>
        println("Message received at processor from service")
        implicit val timeout = Timeout(2 seconds)
        val successF = (catalogueValidator ? ValidateCatalogue(serializedCatalogueItem)).mapTo[Boolean]

        successF.filter(x => x).foreach { _ => 
          println("Message received at processor from validator success")
          cataloguePublisher ! PublishCatalogue(serializedCatalogueItem)
        }

        sender() ! successF
    }
}

object CatalogueProcessor  {
  def props = Props[CatalogueProcessor]
}