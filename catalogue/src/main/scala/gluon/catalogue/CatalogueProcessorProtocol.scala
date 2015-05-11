package gluon.catalogue

import com.goshoplane.common._

import goshoplane.commons.core.protocols.Replyable

sealed trait CatalogueProcessorProtocol

case class ProcessCatalogue(serializedCatalogueItem: SerializedCatalogueItem)
  extends CatalogueProcessorProtocol with Replyable[Boolean]