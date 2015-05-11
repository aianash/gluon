package gluon.service

import scaldi.akka.AkkaInjectable._

import com.typesafe.config.ConfigFactory

import com.twitter.util.Await

import gluon.core.injectors._
import gluon.service.injectors._

import akka.actor.ActorSystem


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

    // injector modules for injecting
    // - ActorSystem
    // - GluonService
    implicit val appModule = new ActorSystemModule(config) :: new GluonModule

    val system = inject [ActorSystem]
    val server = GluonService.start

    // Proper shutting down service
    // and actor system
    scala.sys.addShutdownHook {
      val waitF = server.close()
      Await.ready(waitF)

      system.shutdown
      system.awaitTermination // await until all actors
                              // under system is shut down
    }
  }

}