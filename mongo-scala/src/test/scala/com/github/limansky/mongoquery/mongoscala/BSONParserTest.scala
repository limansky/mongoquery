/*
 * Copyright 2016 Mike Limansky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.limansky.mongoquery.mongoscala

import org.mongodb.scala.bson._
import org.scalatest.{ FlatSpec, Matchers }

class BSONParserTest extends FlatSpec with Matchers {

  "ReactiveMongo parser" should "parse valid BSON" in {
    BSONParser.parse("""{ foo: "bar" }""") should equal(Document("foo" -> "bar"))
    BSONParser.parse("""{ "foo": "bar" }""") should equal(Document("foo" -> "bar"))
  }

  it should "support arrays" in {
    BSONParser.parse("""{ list: ["one", "two"], bar : "baz"}""") should equal(Document(
      "list" -> List("one", "two"),
      "bar" -> "baz"
    ))
  }

  it should "support booleans" in {
    BSONParser.parse("{ bool : false }") should equal(Document("bool" -> BsonBoolean(false)))
  }

  it should "support numbers" in {
    BSONParser.parse("{ int : 5, double : 5.5 }") should equal(Document("int" -> 5, "double" -> 5.5))
  }

  it should "support object id" in {
    BSONParser.parse("""{ _id : ObjectId("1234567890abcdef12345678") }""") should equal(Document(
      "_id" -> BsonObjectId("1234567890abcdef12345678")
    ))
  }

  it should "support null" in {
    BSONParser.parse("{ n : null }") should equal(Document("n" -> BsonNull()))
  }

  it should "support regex" in {
    BSONParser.parse("{ r: /[^abc][abc]/ }") should equal(Document("r" -> BsonRegularExpression("[^abc][abc]", "")))
  }

  it should "support MongoDB operators" in {
    BSONParser.parse("{ age : { $gt : 42 }}") should equal(Document("age" -> Document("$gt" -> 42)))
  }

  it should "throw IllegalArgumentException on malformed BSON" in {
    an[IllegalArgumentException] should be thrownBy BSONParser.parse("{ foo }")
  }
}
