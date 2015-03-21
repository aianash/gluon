package gluon.catalogue.validators

import com.goshoplane.common._

sealed trait ValidationRequest

case class ValidateCatalogue(serializedCatalogueItem: SerializedCatalogueItem) extends ValidationRequest