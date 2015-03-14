include 'common.thrift'

namespace java com.goshoplane.gluon.service

service Gluon {
  bool publish(1:common.SerializedCatalogueItem serializedCatalogueItem)
}