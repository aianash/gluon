package gluon.service

import scala.io.Source
import scala.concurrent._, duration._
import scala.util.control._

import org.rogach.scallop._

import play.api.libs.json._

import scalaz._, Scalaz._

import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodecFactory
import com.twitter.util.{Future => TwitterFuture}

import org.apache.thrift.protocol.TBinaryProtocol

import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod

import com.goshoplane.common._
import com.goshoplane.gluon.service._, Gluon._
import com.goshoplane.cassie.service._

import goshoplane.commons.catalogue._

import scala.concurrent.ExecutionContext.Implicits.global


class PublishStoreInfoConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  val source = opt[String](required = true)
  val target = opt[String](required = true)
  val itemType = opt[String](required = true)

}

object StoreInformationClient {

  def main(args: Array[String]) {
    val cassie = {
      val protocol = new TBinaryProtocol.Factory()
      val client = ClientBuilder().codec(new ThriftClientFramedCodecFactory(None, false, protocol))
        .dest("192.168.1.35:4848").hostConnectionLimit(2).build()

      new Cassie$FinagleClient(client, protocol)
    }

    val conf = new PublishStoreInfoConf(args)

    val itemTypes = conf.itemType().split(",").toSeq

    val storeIdsLF =
      for (line <- Source.fromFile(conf.source()).getLines()) yield {
        val storeJson = Json.parse(line)
        val storeInfo = StoreInfo(
          name      = StoreName(full = ((storeJson \ "name").as[String]).some, handle = ((storeJson \ "handle").as[String]).some).some,
          itemTypes = (itemTypes.flatMap(item => ItemType.valueOf(item))).some,
          address   = PostalAddress(
            gpsLoc  = GPSLocation((storeJson \ "address" \ "gpsLoc" \ "lat").as[Float], (storeJson \ "address" \ "gpsLoc" \ "lon").as[Float]).some,
            title   = ((storeJson \ "address" \ "title").as[String]).some,
            short   = ((storeJson \ "address" \ "short").as[String]).some,
            full    = ((storeJson \ "address" \ "full").as[String]).some,
            pincode = ((storeJson \ "address" \ "pincode").as[String]).some,
            country = ((storeJson \ "address" \ "country").as[String]).some,
            city    = ((storeJson \ "address" \ "city").as[String]).some
          ).some,
          avatar = StoreAvatar(small = ((storeJson \ "avatar" \ "small").as[String]).some, medium = ((storeJson \ "avatar" \ "medium").as[String]).some, large = ((storeJson \ "avatar" \ "large").as[String]).some).some,
          email  = ((storeJson \ "email").as[String]).some,
          phone  = PhoneContact(Seq((storeJson \ "phone").as[String])).some
        )
        println(storeInfo)
        asMethod(cassie.createOrUpdateStore(StoreType.Showroom, storeInfo)).as[Future[StoreId]] andThen {
          case scala.util.Failure(NonFatal(ex)) => ex.printStackTrace
        } map { ((storeJson \ "email").as[String] -> _) }
      }

    val storeIdsF = Future.sequence(storeIdsLF)
    storeIdsF foreach { storeIds =>
      writeToFile(conf.target(), storeIds)
    }
  }

  def writeToFile(fileName: String, storeIds: Iterator[(String, StoreId)]) {
    // PrintWriter
    import java.io._
    val pw = new PrintWriter(new File(fileName))
    storeIds foreach { storeId =>
      println(storeId)
      pw.write(storeId._1 + " : " + storeId._2.stuid.toString + "\n")
    }
    pw.close
  }

}