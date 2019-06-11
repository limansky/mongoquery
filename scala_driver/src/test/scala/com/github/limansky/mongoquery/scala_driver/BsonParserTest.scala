package com.github.limansky.mongoquery.scala_driver

import org.mongodb.scala.bson.{ BsonDocument, BsonNull, BsonObjectId, BsonRegularExpression }
import org.scalatest.{ FlatSpec, Matchers }

class BsonParserTest extends FlatSpec with Matchers {
  "DriverMongo parser" should "parse valid BSON" in {
    BsonParser.parse("""{ foo: "bar" }""") should equal(BsonDocument("foo" -> "bar"))
    BsonParser.parse("""{ "foo": "bar" }""") should equal(BsonDocument("foo" -> "bar"))
  }

  it should "support arrays" in {
    BsonParser.parse("""{ list: ["one", "two"], bar : "baz"}""") should equal(BsonDocument(
      "list" -> List("one", "two"),
      "bar" -> "baz"
    ))
  }

  it should "support booleans" in {
    BsonParser.parse("{ bool : false }") should equal(BsonDocument("bool" -> false))
  }

  it should "support numbers" in {
    BsonParser.parse("{ int : 5, double : 5.5 }") should equal(BsonDocument("int" -> 5, "double" -> 5.5))
  }

  it should "support object id" in {
    BsonParser.parse("""{ _id : ObjectId("1234567890abcdef12345678") }""") should equal(BsonDocument(
      "_id" -> BsonObjectId("1234567890abcdef12345678")
    ))
  }

  it should "support null" in {
    BsonParser.parse("{ n : null }") should equal(BsonDocument("n" -> BsonNull()))
  }

  it should "support regex" in {
    BsonParser.parse("{ r: /[^abc][abc]/ }") should equal(BsonDocument("r" -> BsonRegularExpression("[^abc][abc]", "")))
  }

  it should "support MongoDB operators" in {
    BsonParser.parse("{ age : { $gt : 42 }}") should equal(BsonDocument("age" -> BsonDocument("$gt" -> 42)))
  }

  it should "throw IllegalArgumentException on malformed BSON" in {
    an[IllegalArgumentException] should be thrownBy BsonParser.parse("{ foo }")
  }

  it should "Report invalid ObjectID" in {
    an[IllegalArgumentException] should be thrownBy BsonParser.parse(
      """{ _id : ObjectId("123") }"""
    )
  }

}
