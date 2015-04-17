package gluon.catalogue.validators

import com.goshoplane.common._

import goshoplane.commons.core.protocols.Replyable

sealed trait ValidationRequest

case class ValidateCatalogue(serializedCatalogueItem: SerializedCatalogueItem)
  extends ValidationRequest with Replyable[Boolean]