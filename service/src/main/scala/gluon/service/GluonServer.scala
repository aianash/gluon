package gluon.service

import scaldi.akka.AkkaInjectable._

import com.typesafe.config.ConfigFactory

import com.twitter.finagle.Thrift
import com.twitter.util.Await

import akka.actor.ActorSystem

import gluon.core.injectors._
import gluon.service.injectors._

/**
 * Gluon Server that starts the GluonService
 *
 * {{{
 *   service/target/start gluon.service.GluonServer
 * }}}
 */
object GluonServer {

  def main(args: Array[String]) {
    
    val config = ConfigFactory.load("gluon")

    implicit val appModule = new ActorSystemModule(config) :: new GluonModule

    val service = GluonService.start

    Await.ready(service)
  }

}