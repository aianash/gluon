package gluon.catalogue

import com.goshoplane.common._

sealed trait CatalogueProcessorProtocol

case class ProcessCatalogue(serializedCatalogueItem: SerializedCatalogueItem) extends CatalogueProcessorProtocol