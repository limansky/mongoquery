/*
 * Copyright 2014 Mike Limansky
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

import java.util.Date

import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId
import org.scalatest.{ FlatSpec, Matchers }

class CasbahHelperTest extends FlatSpec with Matchers {

  import com.github.limansky.mongoquery.core.TestObjects._

  "CasbahHelper mq implementation" should "convert string into MongoDBObject" in {
    mq"{ amount : { $$lte : 15}}" should equal(MongoDBObject("amount" -> MongoDBObject("$lte" -> 15)))
  }

  it should "substitute primitive values in the query" in {
    val id = "15B-4"
    val q = mq"{ orderId : $id }"
    q should equal(MongoDBObject("orderId" -> id))
  }

  it should "support nested objects" in {
    val q = mq"""{ user : "Joe", age : {$$gt : 25}}"""
    q should equal(MongoDBObject("user" -> "Joe", "age" -> MongoDBObject("$gt" -> 25)))
  }

  it should "support date references" in {
    val now = new Date
    mq"{start : {$$lte : $now}}" should be(MongoDBObject("start" -> MongoDBObject("$lte" -> now)))
  }

  it should "substitute sequences as arrays in the query" in {
    val colors = List("red", "green", "blue")
    val q = mq"{ color : {$$in : $colors}}"
    q should equal(MongoDBObject("color" -> MongoDBObject("$in" -> colors)))
  }

  it should "support arrays of objects" in {
    val q = mq"""{ phones : [ { number : "223322", type : "home"}, { number: "332233", type: "work"} ] }"""
    q should equal(MongoDBObject("phones" -> List(MongoDBObject("number" -> "223322", "type" -> "home"), MongoDBObject("number" -> "332233", "type" -> "work"))))
  }

  it should "support arrays of arrays" in {
    val q = mq"""{ indexes : [ [1,3,5], [4,6,7]] }"""
    q should equal(MongoDBObject("indexes" -> List(List(1, 3, 5), List(4, 6, 7))))
  }

  it should "be possible to compose queries" in {
    val sub = mq"{$$gt : 10}"
    val q = mq"{price : $sub}"
    q should equal(MongoDBObject("price" -> MongoDBObject("$gt" -> 10)))
  }

  it should "support ObjectIds injection" in {
    val id = ObjectId.get
    val q = mq"{ clientId : $id }"
    q should equal(MongoDBObject("clientId" -> id))
  }

  it should "support ObjectId literals" in {
    val q = mq"""{employeeId : ObjectId("00112233445566778899aabb")}"""
    q should equal(MongoDBObject("employeeId" -> new ObjectId("00112233445566778899aabb")))
  }

  it should "support Boolean literals" in {
    mq"{foo : false}" should equal(MongoDBObject("foo" -> false))
  }

  it should "support null" in {
    mq"{bar : null}" should equal(MongoDBObject("bar" -> null))
  }

  it should "support empty objects" in {
    mq"{}" should equal(MongoDBObject.empty)
  }

  it should "handle regular expressions" in {
    mq"{ s : /^ba/ }" should equal(MongoDBObject("s" -> "^ba".r))
  }

  "CasbahHelper mqt implementation" should "pass valid object" in {
    mqt"{ s : 'test' }"[Foo] should equal(MongoDBObject("s" -> "test"))
    mqt"{ i : 5, f.s : 'test' }"[Bar] should equal(MongoDBObject("i" -> 5, "f.s" -> "test"))
  }

  it should "ignore operators" in {
    mqt"{ $$set : { s : 'aaa'}}"[Foo] should equal(MongoDBObject("$set" -> MongoDBObject("s" -> "aaa")))
  }

  it should "handle options" in {
    mqt"{ d : 3.14, f.s : 'bbb'}"[Baz] should equal(MongoDBObject("d" -> 3.14, "f.s" -> "bbb"))
  }

  it should "handle collections" in {
    mqt"{ s : 'ccc', lf.s : 'zzzz' }"[Quux] should equal(MongoDBObject("s" -> "ccc", "lf.s" -> "zzzz"))
    mqt"{ s : 'ccc', lf.1.s : 'zzzz' }"[Quux] should equal(MongoDBObject("s" -> "ccc", "lf.1.s" -> "zzzz"))
    mqt"{ s : 'ccc', lf.$$.s : 'zzzz' }"[Quux] should equal(MongoDBObject("s" -> "ccc", "lf.$.s" -> "zzzz"))
  }
}
