package gluon.service

import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.util.{Future => TwitterFuture, Await}

import org.apache.thrift.protocol.TBinaryProtocol

import com.goshoplane.common._
import com.goshoplane.gluon.service._

import goshoplane.commons.catalogue._



object TestGluonClient {

  import ItemTypeGroup._

  def main(args: Array[String]) {

    val client = ClientBuilder().codec(ThriftClientFramedCodec())
      .dest("127.0.0.1:2106").hostConnectionLimit(2).build()

    val gluon = new Gluon$FinagleClient(client, new TBinaryProtocol.Factory())

    // testing serialization and deserialization
    val storeId = StoreId(18273L)

    val clothingItem = ClothingItem(
      itemId         = CatalogueItemId(storeId = storeId, cuid = 198003L),
      itemType       = ItemType.ApparelMen,
      itemTypeGroups = ItemTypeGroups(Array(Clothing, MenClothing, Jeans)),
      namedType      = NamedType("men's jeans"),
      productTitle   = ProductTitle("HRX Men Black Indigo Dyed Jeans"),
      colors         = Colors(Array("black", "blue")),
      sizes          = Sizes(Array("32", "34")),
      brand          = Brand("levis men's jeans"),
      clothingType   = ClothingType("men's jeans"),
      description    = Description("new cool fabric"),
      price          = Price(1977)
    )

    val successF = gluon.publish(CatalogueItem.encode(clothingItem).get)

    successF onSuccess {
      case res => println(res)
    } onFailure {
      case ex: Exception => ex.printStackTrace()
    } ensure { client.close() }

  }

}