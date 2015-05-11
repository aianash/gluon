package gluon.catalogue.publishers

import com.goshoplane.common._

import goshoplane.commons.core.protocols.Replyable

sealed trait PublisherProtocol

case class PublishCatalogue(serializedCatalogueItem: SerializedCatalogueItem)
  extends PublisherProtocol