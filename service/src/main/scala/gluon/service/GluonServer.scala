package gluon.service

import scaldi.akka.AkkaInjectable._

import com.typesafe.config.ConfigFactory

import com.twitter.finagle.Thrift
import com.twitter.util.Await

import akka.actor.ActorSystem

import gluon.service._
/**
 * Gluon Server that starts the GluonService
 *
 * {{{
 *   service/target/start gluon.service.GluonServer
 * }}}
 */
object GluonServer {

  def main(args: Array[String]) {

    val service = GluonFinagleService(new GluonService, new TBinaryProtocol.Factory());
    Await.ready(service)
  }

}