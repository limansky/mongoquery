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

package com.github.limansky.mongoquery.casbah

import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import org.scalatest.{ FlatSpec, Matchers }

class CasbahParserTest extends FlatSpec with Matchers {

  "Casbah parser" should "parse valid BSON" in {
    CasbahParser.parse("""{ foo: "bar" }""") should equal(MongoDBObject("foo" -> "bar"))
    CasbahParser.parse("""{ "foo": "bar" }""") should equal(MongoDBObject("foo" -> "bar"))
  }

  it should "support arrays" in {
    CasbahParser.parse("""{ list: ["one", "two"], bar : "baz"}""") should equal(MongoDBObject(
      "list" -> List("one", "two"),
      "bar" -> "baz"
    ))
  }

  it should "support booleans" in {
    CasbahParser.parse("{ bool : false }") should equal(MongoDBObject("bool" -> false))
  }

  it should "support numbers" in {
    CasbahParser.parse("{ int : 5, double : 5.5 }") should equal(MongoDBObject("int" -> 5, "double" -> 5.5))
  }

  it should "support object id" in {
    CasbahParser.parse("""{ _id : ObjectId("1234567890abcdef12345678") }""") should equal(MongoDBObject(
      "_id" -> new ObjectId("1234567890abcdef12345678")
    ))
  }

  it should "support null" in {
    CasbahParser.parse("{ n : null }") should equal(MongoDBObject("n" -> null))
  }

  it should "support regex" in {
    CasbahParser.parse("{ r : /abc/ }") should equal(MongoDBObject("r" -> "abc".r))
  }

  it should "throw IllegalArgumentException on malformed BSON" in {
    an[IllegalArgumentException] should be thrownBy CasbahParser.parse("{ foo }")
  }

}
