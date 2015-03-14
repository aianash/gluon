package gluon.service

import com.twitter.finagle.Thrift
import com.twitter.util.{Future => TwitterFuture, Await}

import java.nio.ByteBuffer
import com.goshoplane.common._
import com.goshoplane.gluon.service._

object TestGluonClient {
  def main(args: Array[String]) {

      val client = Thrift.newIface[Gluon.FutureIface]("localhost:2106")

      val stId = StoreId(1L, StoreType.Clothing)
      val catId = CatalogueItemId(1L, stId)
      val serId = SerializerId("serializer", SerializerType.Json)
      val bb = ByteBuffer.wrap(Array[Byte](10));

      val ret = client.publish(SerializedCatalogueItem(catId, serId, bb))

      Await.ready(ret)

      ret onSuccess {
        case res => println(res)
      }
      ret onFailure {
        case ex: Exception => ex.printStackTrace
      }

    }


}