package gluon.catalogue.publishers

import com.goshoplane.common._

sealed trait PublisherProtocol

case class PublishCatalogue(serializedCatalogueItem: SerializedCatalogueItem) extends PublisherProtocol