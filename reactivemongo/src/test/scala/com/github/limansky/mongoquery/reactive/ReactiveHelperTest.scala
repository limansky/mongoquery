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

package com.github.limansky.mongoquery.reactive

import java.util.Date

import org.scalatest.{ FlatSpec, Matchers }
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONNull, BSONObjectID, BSONRegex }

class ReactiveHelperTest extends FlatSpec with Matchers {

  import com.github.limansky.mongoquery.core.TestObjects._

  "ReactiveHelper mq implementation" should "convert string into BSONDocument" in {
    val q = mq"{ amount : { $$lte : 15}}"
    q should equal(BSONDocument("amount" -> BSONDocument("$lte" -> 15)))
  }

  it should "substitute primitive values in the query" in {
    val id = "15B-4"
    mq"{ orderId : $id }" should equal(BSONDocument("orderId" -> id))
  }

  it should "support nested objects" in {
    val q = mq"""{ user : "Joe", age : {$$gt : 25}}"""
    q should equal(BSONDocument("user" -> "Joe", "age" -> BSONDocument("$gt" -> 25)))
  }

  it should "substitute sequences as arrays in the query" in {
    val colors = List("red", "green", "blue")
    val q = mq"{ color : {$$in : $colors}}"
    q should equal(BSONDocument("color" -> BSONDocument("$in" -> colors)))
  }

  it should "substitute dates" in {
    val now = BSONDateTime(new Date().getTime)
    mq"{start : {$$lte : $now}}" should be(BSONDocument("start" -> BSONDocument("$lte" -> now)))
  }

  it should "support arrays of objects" in {
    val q = mq"""{ phones : [ { number : "223322", type : "home"}, { number: "332233", type: "work"} ] }"""
    q should equal(BSONDocument("phones" -> List(BSONDocument("number" -> "223322", "type" -> "home"), BSONDocument("number" -> "332233", "type" -> "work"))))
  }

  it should "support arrays of arrays" in {
    val q = mq"""{ indexes : [ [1,3,5], [4,6,7]] }"""
    q should equal(BSONDocument("indexes" -> List(List(1, 3, 5), List(4, 6, 7))))
  }

  it should "be possible to compose queries" in {
    val sub = mq"{$$gt : 10}"
    mq"{price : $sub}" should equal(BSONDocument("price" -> BSONDocument("$gt" -> 10)))
  }

  it should "support BSONObjectIDs injection" in {
    val id = BSONObjectID.generate
    mq"{ clientId : $id }" should equal(BSONDocument("clientId" -> id))
  }

  it should "support BSONObjectIDs literals" in {
    val q = mq"""{ clientId : ObjectId("aabbccddeeff112233445566") }"""
    q should equal(BSONDocument("clientId" -> BSONObjectID.parse("aabbccddeeff112233445566").get))
  }

  it should "support boolean literals" in {
    mq"{bar : true}" should equal(BSONDocument("bar" -> true))
  }

  it should "support null" in {
    mq"{bar : null}" should equal(BSONDocument("bar" -> BSONNull))
  }

  it should "support empty objects" in {
    mq"{}" should equal(BSONDocument.empty)
  }

  it should "handle regular expressions" in {
    mq"{ s : /^ba/ }" should equal(BSONDocument("s" -> BSONRegex("^ba", "")))
    mq"{ a : /mew/i }" should equal(BSONDocument("a" -> BSONRegex("mew", "i")))
  }

  "ReactiveHelper mqt implementation" should "pass valid object" in {
    mqt"{ s : 'test' }"[Foo] should equal(BSONDocument("s" -> "test"))
    mqt"{ i : 5, f.s : 'test' }"[Bar] should equal(BSONDocument("i" -> 5, "f.s" -> "test"))
  }

  it should "ignore operators" in {
    mqt"{ $$set : { s : 'aaa'}}"[Foo] should equal(BSONDocument("$set" -> BSONDocument("s" -> "aaa")))
  }

  it should "handle options" in {
    mqt"{ d : 3.14, f.s : 'bbb'}"[Baz] should equal(BSONDocument("d" -> 3.14, "f.s" -> "bbb"))
  }

  it should "handle collections" in {
    mqt"{ s : 'ccc', lf.s : 'zzzz' }"[Quux] should equal(BSONDocument("s" -> "ccc", "lf.s" -> "zzzz"))
    mqt"{ s : 'ccc', lf.1.s : 'zzzz' }"[Quux] should equal(BSONDocument("s" -> "ccc", "lf.1.s" -> "zzzz"))
    mqt"{ s : 'ccc', lf.$$.s : 'zzzz' }"[Quux] should equal(BSONDocument("s" -> "ccc", "lf.$.s" -> "zzzz"))
  }
}
