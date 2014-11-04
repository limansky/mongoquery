package mongoquery

import casbah._
import org.scalatest.FlatSpec
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.Matchers

class CasbahHelperTest extends FlatSpec with Matchers {

  "CasbahHelper" should "convert string into MongoDBObject" in {

    val q: MongoDBObject = mq"{ amount : { $$lte : 15}}"
    q should equal(MongoDBObject("amount" -> MongoDBObject("$lte" -> 15)))
  }

  it should "substitute primitive values in the query" in {
    val id = "15B-4"
    val q: MongoDBObject = mq"{ orderId : $id }"
    q should equal(MongoDBObject("orderId" -> id))
  }

  it should "support nested objects" in {
    val q: MongoDBObject = mq"""{ user : "Joe", age : {$$gt : 25}}"""
    q should equal(MongoDBObject("user" -> "Joe", "age" -> MongoDBObject("$gt" -> 25)))
  }

  it should "substitute sequences as arrays in the query" in {
    val colors = List("red", "green", "blue")
    val q: MongoDBObject = mq"{ color : {$$in : $colors}}"
    q should equal(MongoDBObject("color" -> MongoDBObject("$in" -> colors)))
  }

  it should "support arrays of objects" in {
    val q: MongoDBObject = mq"""{ phones : [ { number : "223322", type : "home"}, { number: "332233", type: "work"} ] }"""
    q should equal(MongoDBObject("phones" -> List(MongoDBObject("number" -> "223322", "type" -> "home"), MongoDBObject("number" -> "332233", "type" -> "work"))))
  }

  it should "support arrays of arrays" in {
    val q: MongoDBObject = mq"""{ indexes : [ [1,3,5], [4,6,7]] }"""
    q should equal(MongoDBObject("indexes" -> List(List(1, 3, 5), List(4, 6, 7))))
  }

  it should "be possible to compose queries" in {
    val sub = mq"{$$gt : 10}"
    val q: MongoDBObject = mq"{price : $sub}"
    q should equal(MongoDBObject("price" -> MongoDBObject("$gt" -> 10)))
  }
}
