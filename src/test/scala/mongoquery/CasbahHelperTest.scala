package mongoquery

import casbah._
import org.scalatest.FlatSpec
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.Matchers

class CasbahHelperTest extends FlatSpec with Matchers {

  "CasbahHelper" should "convert string into MongoDBObject" in {
    mq"{ amount : { $$lte : 15}}" should equal (MongoDBObject("amount" -> MongoDBObject("$lte" -> 15)))
  }

  it should "substitute values in the query" in {
    val id = "15B-4"
    mq"{ orderId : $id }" should equal (MongoDBObject("orderId" -> id))

    val colors = List("red", "green", "blue")
    mq"{ color : {$$in : $colors}}" should equal (MongoDBObject("color" -> MongoDBObject("$in" -> colors)))
  }
}