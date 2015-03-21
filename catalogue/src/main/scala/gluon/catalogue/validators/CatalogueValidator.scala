package gluon.catalogue.validators

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.concurrent._, duration._

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

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.finagle.Thrift
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod

import com.typesafe.config.{Config, ConfigFactory}

class CatalogueValidator extends Actor {
  def receive = {
    case ValidateCatalogue(serializedCatalogueItem) =>
      println("Message received at validator from processor")
      sender() ! true
  }
}

object CatalogueValidator {
  def props = Props[CatalogueValidator]
}