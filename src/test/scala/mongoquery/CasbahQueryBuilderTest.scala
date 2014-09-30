package mongoquery

import org.scalatest.FlatSpec
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.Matchers

class CasbahQueryBuilderTest extends FlatSpec with Matchers {

  val builder = new CasbahQueryBuilder

  "CasbahQueryBuilder" should "build simple query" in  {
    builder.build("{ clientId : 45 }") should equal(MongoDBObject("clientId" -> 45))
  }

  it should "create MongoDBObject from string" in {

    builder.build("{ user : \"Joe\", age : {$gt : 25}}") should equal(
        MongoDBObject("user" -> "Joe", "age" -> MongoDBObject("$gt"-> 25)))
  }
}
