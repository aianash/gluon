package gluon.service

import scaldi.Module
import scaldi.Injectable._

package object injectors {
  class GluonModule extends Module {
    bind  [GluonService] to new GluonService
  }
}