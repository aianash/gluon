package gluon.service

import scaldi.akka.AkkaInjectable._

import com.typesafe.config.ConfigFactory

import com.twitter.util.Await

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

    val server = GluonService.start

    scala.sys.addShutdownHook {
      val waitF = server.close()
      Await.ready(waitF)
    }
  }

}