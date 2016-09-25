package com.github.limansky.mongoquery.reactive

import org.scalatest.{ FlatSpec, Matchers }
import reactivemongo.bson.{ BSONDocument, BSONNull, BSONObjectID, BSONRegex }

class BSONParserTest extends FlatSpec with Matchers {
  "ReactiveMongo parser" should "parse valid BSON" in {
    BSONParser.parse("""{ foo: "bar" }""") should equal(BSONDocument("foo" -> "bar"))
    BSONParser.parse("""{ "foo": "bar" }""") should equal(BSONDocument("foo" -> "bar"))
  }

  it should "support arrays" in {
    BSONParser.parse("""{ list: ["one", "two"], bar : "baz"}""") should equal(BSONDocument(
      "list" -> List("one", "two"),
      "bar" -> "baz"
    ))
  }

  it should "support booleans" in {
    BSONParser.parse("{ bool : false }") should equal(BSONDocument("bool" -> false))
  }

  it should "support numbers" in {
    BSONParser.parse("{ int : 5, double : 5.5 }") should equal(BSONDocument("int" -> 5, "double" -> 5.5))
  }

  it should "support object id" in {
    BSONParser.parse("""{ _id : ObjectId("1234567890abcdef12345678") }""") should equal(BSONDocument(
      "_id" -> BSONObjectID("1234567890abcdef12345678")
    ))
  }

  it should "support null" in {
    BSONParser.parse("{ n : null }") should equal(BSONDocument("n" -> BSONNull))
  }

  it should "support regex" in {
    BSONParser.parse("{ r: /[^abc][abc]/ }") should equal(BSONDocument("r" -> BSONRegex("[^abc][abc]", "")))
  }

  it should "support MongoDB operators" in {
    BSONParser.parse("{ age : { $gt : 42 }}") should equal(BSONDocument("age" -> BSONDocument("$gt" -> 42)))
  }

  it should "throw IllegalArgumentException on malformed BSON" in {
    an[IllegalArgumentException] should be thrownBy BSONParser.parse("{ foo }")
  }

}
