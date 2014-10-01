package mongoquery

import org.scalatest.FlatSpec
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.Matchers

class CasbahQueryBuilderTest extends FlatSpec with Matchers {

  "CasbahQueryBuilder" should "build simple query" in  {
    CasbahQueryBuilder.build("{ clientId : 45 }") should equal(MongoDBObject("clientId" -> 45))
  }

  it should "create MongoDBObject from string" in {

    CasbahQueryBuilder.build("{ user : \"Joe\", age : {$gt : 25}}") should equal(
        MongoDBObject("user" -> "Joe", "age" -> MongoDBObject("$gt"-> 25)))
  }

  it should "support arrays of objects" in {
    CasbahQueryBuilder.build("""{ phones : [ { number : "223322", type : "home"}, { number: "332233", type: "work"} ] }""") should equal(
        MongoDBObject("phones" -> List(MongoDBObject("number" -> "223322", "type" -> "home"), MongoDBObject("number" -> "332233", "type" -> "work"))))
  }

  it should "support arrays of arrays" in {
    CasbahQueryBuilder.build("""{ indexes : [ [1,3,5], [4,6,7]] }""") should equal(
        MongoDBObject("indexes" -> List(List(1,3,5), List(4,6,7))))
  }
}
