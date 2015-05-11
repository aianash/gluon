package gluon.core

import scaldi.Module
import scaldi.Injectable._

import akka.actor.ActorSystem

import com.typesafe.config.Config

package object injectors {

  // Injector for gluon's ActorSystem. dont
  // directly use it for other ActorSystem with different
  // name
  class ActorSystemModule(config: Config) extends Module {
    bind [ActorSystem] to ActorSystem(config.getString("gluon.actorSystem"), config)
  }
}