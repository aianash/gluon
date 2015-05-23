package gluon.service

import scala.util.Random
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.math._
import scala.util.matching._
import scala.util.Failure

import play.api.libs.json._

import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodecFactory
import com.twitter.util.{Future => TwitterFuture, Await}

import org.apache.thrift.protocol.TBinaryProtocol

import org.rogach.scallop._

import com.goshoplane.common._
import com.goshoplane.gluon.service._, Gluon._
import com.goshoplane.cassie.service._

import goshoplane.commons.catalogue._

import scalaz._, Scalaz._

import scala.io.Source

import scalaj.http._

import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod


import scala.concurrent.ExecutionContext.Implicits.global


class PublishCatalogueItemInfoConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  val source = opt[String](required = true)
  val storeId = opt[String](required = true)
  val itemType = opt[String](required = true)
  val itemTypeGroups = opt[String](required = true)
  val brand = opt[String](required = true)
  val namedType = opt[String](required = true)

}

/**
 * A sample test client
 * [NOTE] this is no, unit test case, therefore
 * actually interact with all the systems involved
 *
 * Will be replaced by unit test cases soon
 */
object TestGluonClient {

  import ItemTypeGroup._


  def main(args: Array[String]) {

    val gluon = {
      val protocol = new TBinaryProtocol.Factory()
      val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
        .dest("192.168.1.35:2106").hostConnectionLimit(2).build()

      new Gluon$FinagleClient(client, protocol)
    }

    val conf = new PublishCatalogueItemInfoConf(args)

    val itemTypeGroups = conf.itemTypeGroups().split(",").toSeq

    val re = new Regex("-?\\d+")

    val successesLF =
      Source.fromFile(conf.source()).getLines() flatMap { line =>
        val itemJson = Json.parse(line)
        val colors = (itemJson \ "color").asOpt[String].map(Seq(_)) orElse (itemJson \ "colors").asOpt[Seq[String]]
        val sizes = (itemJson \ "size").asOpt[String].map(Seq(_)) orElse (itemJson \ "sizes").asOpt[Seq[String]]
        val url = (itemJson \ "image").as[String]
        val result = Http("https://api.imageshack.com/v2/images").postForm(Seq(("api_key" -> "8CDEHKTUb804f86345cceaf9381182e652b06be9"), ("urls" -> url))).asString
        val response = Json.parse(result.body)
        val is = ((response \ "result" \ "images")(0) \ "server").as[Int]
        val name = ((response \ "result" \ "images")(0) \ "filename").as[String]
        val smallImageUrl = "imagizer.imageshack.us/100xf/" + is + "/" + name
        val mediumImageUrl = "imagizer.imageshack.us/200xf/" + is + "/" + name
        val largeImageUrl = "imagizer.imageshack.us/400xf/" + is + "/" + name


        val item = ClothingItem(
          itemId         = CatalogueItemId(storeId = StoreId(conf.storeId().toLong), cuid = System.currentTimeMillis + Random.nextLong),
          itemType       = ItemType.valueOf(conf.itemType()).get,
          itemTypeGroups = ItemTypeGroups(itemTypeGroups.map(group => ItemTypeGroup.withName(group))),
          namedType      = NamedType(conf.namedType()),
          productTitle   = ProductTitle((itemJson \ "title").as[String]),
          colors         = Colors(colors.getOrElse(Seq.empty[String])),
          sizes          = Sizes(sizes.getOrElse(Seq.empty[String])),
          brand          = Brand((itemJson \ "brand").asOpt[String].getOrElse(conf.brand())),
          description    = Description((itemJson \ "description").asOpt[String].getOrElse("")),
          price          = re.findFirstIn((itemJson \ "price").as[String]).map(x => Price(x.toFloat)).getOrElse(Price(0)),
          fabric         = ApparelFabric((itemJson \ "fabric").asOpt[String].getOrElse("")),
          style          = ApparelStyle((itemJson \ "style").asOpt[String].getOrElse("")),
          fit            = ApparelFit((itemJson \ "fit").asOpt[String].getOrElse("")),
          productImage   = ProductImage(small = smallImageUrl, medium = mediumImageUrl, large = largeImageUrl)
        )
        println(item + "\n\n")
        CatalogueItem.encode(item).map { serItem =>
          println(serItem)
          asMethod(gluon.publish(serItem)).as[Future[Boolean]]  andThen {
            case Failure(ex) => ex.printStackTrace
          }
        }
      }

      Future.reduce(successesLF)(_ && _) foreach { _ => println("All data persisted successfully") }

  }

}