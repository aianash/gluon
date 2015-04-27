package gluon.service

import scala.util.Random

import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodecFactory
import com.twitter.util.{Future => TwitterFuture, Await}

import org.apache.thrift.protocol.TBinaryProtocol

import com.goshoplane.common._
import com.goshoplane.gluon.service._, Gluon._
import com.goshoplane.cassie.service._

import goshoplane.commons.catalogue._


import scalaz._, Scalaz._

object TestGluonClient {

  import ItemTypeGroup._

  def main(args: Array[String]) {

    val protocol = new TBinaryProtocol.Factory()
    val cassie = {
      val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
        .dest("127.0.0.1:4848").hostConnectionLimit(2).build()

      new Cassie$FinagleClient(client, protocol)
    }

    val info = StoreInfo(
      name      = StoreName(full = "Levis Showroom".some, handle = "levis".some).some,
      itemTypes = Seq(ItemType.ClothingItem).some,
      address   = PostalAddress(
        gpsLoc = GPSLocation(100.0, 30.0).some,
        title  = "kormangla".some,
        short  = "kormangla".some,
        full   = "kormangla".some,
        pincode = "560027".some,
        country = "India".some,
        city = "bangalore".some
      ).some,
      avatar = StoreAvatar(small = "asdf".some, medium = "asdfas".some, large = "asdfasdf".some).some,
      email  = ("levis" + Random.nextInt + "@gmail.com").some,
      phone  = PhoneContact(Seq("999388200029")).some
    )

    val storeIdF = cassie.createOrUpdateStore(StoreType.Showroom, info)


    val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
      .dest("127.0.0.1:2106").hostConnectionLimit(2).build()

    val gluon = new Gluon$FinagleClient(client, protocol)

    storeIdF onSuccess {
      case res => println(res)
    } onFailure {
      case ex: Exception => ex.printStackTrace()
    } ensure { client.close() }


    storeIdF foreach { storeId =>

      println("Created store " + storeId)

      val clothingItem = ClothingItem(
        itemId         = CatalogueItemId(storeId = storeId, cuid = System.currentTimeMillis),
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

      // successF onSuccess {
      //   case res => println(res)
      // } onFailure {
      //   case ex: Exception => ex.printStackTrace()
      // } ensure { client.close() }

    }

  }

}