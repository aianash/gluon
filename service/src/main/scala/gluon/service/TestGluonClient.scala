package gluon.service

import scala.util.Random
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.math._

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

import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod


import scala.concurrent.ExecutionContext.Implicits.global



object TestGluonClient {

  import ItemTypeGroup._

  val leviskormangla = StoreInfo(
    name      = StoreName(full = "Levis Showroom".some, handle = "leviskormangla".some).some,
    itemTypes = Seq(ItemType.ClothingItem).some,
    address   = PostalAddress(
      gpsLoc  = GPSLocation(12.9336479, 77.6300757).some,
      title   = "8FT Main Road, Koramangla".some,
      short   = "8FT Main Road, Koramangala 4-C Block".some,
      full    = "744, 8FT Main Road, Koramangala 4-C Block, Koramangla, Bengaluru, Karnataka".some,
      pincode = "560095".some,
      country = "India".some,
      city    = "Bengaluru".some
    ).some,
    avatar = StoreAvatar(small = "asdf".some, medium = "asdfas".some, large = "asdfasdf".some).some,
    email  = ("levis-kormangla@gmail.com").some,
    phone  = PhoneContact(Seq("08197417012")).some
  )

  val levismgroad = StoreInfo(
    name      = StoreName(full = "Levis Strauss Signature".some, handle = "levismgroad".some).some,
    itemTypes = Seq(ItemType.ClothingItem).some,
    address   = PostalAddress(
      gpsLoc  = GPSLocation(12.9715282, 77.6070446).some,
      title   = "Arcade Mall, Brigade Road".some,
      short   = "Mota Royal Arcade Mall, Brigade Road".some,
      full    = "Ground Floor, Mota Royal Arcade Mall, Brigade Road, Bengaluru, Karnataka".some,
      pincode = "560024".some,
      country = "India".some,
      city    = "Bengaluru".some
    ).some,
    avatar = StoreAvatar(small = "asdf".some, medium = "asdfas".some, large = "asdfasdf".some).some,
    email  = ("levis-mgroad@gmail.com").some,
    phone  = PhoneContact(Seq("08041558372")).some
  )

  val levisindiranagar = StoreInfo(
    name      = StoreName(full = "Levis Show Room".some, handle = "levisindiranagar".some).some,
    itemTypes = Seq(ItemType.ClothingItem).some,
    address   = PostalAddress(
      gpsLoc  = GPSLocation(12.9814534, 77.6102515).some,
      title   = "Kamaraj Road".some,
      short   = "Kamaraj Rd Tasker Town, Sivanchetti Gardens".some,
      full    = "Kamaraj Rd Tasker Town, Sivanchetti Gardens, Bengaluru, Karnataka".some,
      pincode = "560001".some,
      country = "India".some,
      city    = "Bengaluru".some
    ).some,
    avatar = StoreAvatar(small = "asdf".some, medium = "asdfas".some, large = "asdfasdf".some).some,
    email  = ("levis-indiranagar@gmail.com").some,
    phone  = PhoneContact(Seq("08042109507")).some
  )


  val storeInfos = Seq(leviskormangla, levismgroad, levisindiranagar)

  val levisjeans1 = ClothingItem(
    itemId         = CatalogueItemId(storeId = StoreId(-1L), cuid = System.currentTimeMillis),
    itemType       = ItemType.ClothingItem,
    itemTypeGroups = ItemTypeGroups(Array(Clothing, MenClothing, Jeans)),
    namedType      = NamedType("men's jeans"),
    productTitle   = ProductTitle("M Black mamba"),
    colors         = Colors(Array("black", "blue")),
    sizes          = Sizes(Array("32", "34")),
    brand          = Brand("levis men's jeans"),
    description    = Description("sits below waist"),
    price          = Price(1977),
    fabric         = ApparelFabric("Denim"),
    style          = ApparelStyle("slightly tappered leg"),
    fit            = ApparelFit("Slim"),
    productImage   = ProductImage("asdf", "asdfasf", "asdfasdf")
  )

  val levisjeans2 = ClothingItem(
    itemId         = CatalogueItemId(storeId = StoreId(-1L), cuid = System.currentTimeMillis),
    itemType       = ItemType.ClothingItem,
    itemTypeGroups = ItemTypeGroups(Array(Clothing, MenClothing, Jeans)),
    namedType      = NamedType("men's jeans"),
    productTitle   = ProductTitle("Pirate Black"),
    colors         = Colors(Array("black", "blue")),
    sizes          = Sizes(Array("32", "34")),
    brand          = Brand("levis men's jeans"),
    description    = Description("This pair of Levi's® Jeans is part of our Water<Less™ program that minimizes water in the finishing process."),
    price          = Price(1977),
    fabric         = ApparelFabric("poly cotton"),
    style          = ApparelStyle("slightly tappered leg"),
    fit            = ApparelFit("Slim"),
    productImage   = ProductImage("asdf", "asdfasf", "asdfasdf")
  )

  val levisjeans3 = ClothingItem(
    itemId         = CatalogueItemId(storeId = StoreId(-1L), cuid = System.currentTimeMillis),
    itemType       = ItemType.ClothingItem,
    itemTypeGroups = ItemTypeGroups(Array(Clothing, MenClothing, Jeans)),
    namedType      = NamedType("men's jeans"),
    productTitle   = ProductTitle("Slate Black Twill"),
    colors         = Colors(Array("black", "blue")),
    sizes          = Sizes(Array("32", "34")),
    brand          = Brand("levis men's jeans"),
    description    = Description("sits below waist"),
    price          = Price(2799),
    fabric         = ApparelFabric("Rinsed Twill"),
    style          = ApparelStyle("slightly tappered leg"),
    fit            = ApparelFit("Slim"),
    productImage   = ProductImage("asdf", "asdfasf", "asdfasdf")
  )



  def main(args: Array[String]) {

    val cassie = {
      val protocol = new TBinaryProtocol.Factory()
      val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
        .dest("127.0.0.1:4848").hostConnectionLimit(2).build()

      new Cassie$FinagleClient(client, protocol)
    }

    val gluon = {
      val protocol = new TBinaryProtocol.Factory()
      val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
        .dest("127.0.0.1:2106").hostConnectionLimit(2).build()

      new Gluon$FinagleClient(client, protocol)
    }


    val storeIdsF =
      Future.sequence(storeInfos.map(info =>
        asMethod(cassie.createOrUpdateStore(StoreType.Showroom, info)).as[Future[StoreId]]))

    storeIdsF onFailure { case NonFatal(ex) => ex.printStackTrace }

    storeIdsF foreach { storeIds =>
      val successesF = Future.sequence(storeIds map { storeId =>
        println("Created store " + storeId)

        val item1 = CatalogueItem.encode(levisjeans1.copy(itemId = CatalogueItemId(storeId = storeId, cuid = abs(System.currentTimeMillis + Random.nextLong) ))).get
        val item2 = CatalogueItem.encode(levisjeans2.copy(itemId = CatalogueItemId(storeId = storeId, cuid = abs(System.currentTimeMillis + Random.nextLong) ))).get
        val item3 = CatalogueItem.encode(levisjeans3.copy(itemId = CatalogueItemId(storeId = storeId, cuid = abs(System.currentTimeMillis + Random.nextLong) ))).get

        println(s"Publishing items = ${item1.itemId}, ${item2.itemId}, ${item3.itemId}")

        asMethod(for {
          _       <- gluon.publish(item1)
          _       <- gluon.publish(item2)
          success <- gluon.publish(item3)
        } yield success).as[Future[Boolean]]
      })

      successesF foreach { _ => println("Data persisted successfully") }
    }

  }

}