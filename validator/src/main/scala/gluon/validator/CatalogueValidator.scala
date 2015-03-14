package gluon.validator

import scala.util._, concurrent._

import akka.actor.{Actor, Props}
import akka.pattern.pipe;

import com.goshoplane.common._
import ExecutionContext.Implicits.global

class CatalogueValidator extends Actor {
  def receive = {
    case ValidateCatalogue(serializedCatalogueItem: SerializedCatalogueItem) => 

  }
}

object CatalogueValidator {
  def props = Props[CatalogueValidator]
}