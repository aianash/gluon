package gluon.service

import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodecFactory
import com.twitter.util.{Future => TwitterFuture, Await}

import org.apache.thrift.protocol.TBinaryProtocol

import com.goshoplane.common._
import com.goshoplane.gluon.service._, Gluon._

import goshoplane.commons.catalogue._



object TestGluonClient {

  import ItemTypeGroup._

  def main(args: Array[String]) {

    val protocol = new TBinaryProtocol.Factory()
    val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
      .dest("127.0.0.1:2106").hostConnectionLimit(2).build()

    val gluon = new Gluon$FinagleClient(client, protocol)

    // testing serialization and deserialization
    val storeId = StoreId(18273L)

    val clothingItem = ClothingItem(
      itemId         = CatalogueItemId(storeId = storeId, cuid = 198003L),
      itemType       = ItemType.ClothingItem,
      itemTypeGroups = ItemTypeGroups(Array(Clothing, MenClothing, Jeans)),
      namedType      = NamedType("men's jeans"),
      productTitle   = ProductTitle("HRX Men Black Indigo Dyed Jeans"),
      colors         = Colors(Array("black", "blue")),
      sizes          = Sizes(Array("32", "34")),
      brand          = Brand("levis men's jeans"),
      description    = Description("new cool fabric"),
      price          = Price(1977),
      fabric         = ApparelFabric("fabric"),
      style          = ApparelStyle("asdf"),
      fit            = ApparelFit("asdfs"),
      productImage   = ProductImage("asdf", "asdfasf", "asdfasdf")
    )

    val encoded = CatalogueItem.encode(clothingItem).get
    println(CatalogueItem.decode(encoded))

    val successF = gluon.publish(encoded)

    successF onSuccess {
      case res => println(res)
    } onFailure {
      case ex: Exception => ex.printStackTrace()
    } ensure { client.close() }

  }

}