/*
 * Copyright 2015 Mike Limansky
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

package com.github.limansky.mongoquery.tepkin

import java.util.Date

import org.scalatest.{ FlatSpec, Matchers }
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element.BsonObjectId

class TepkinHelperTest extends FlatSpec with Matchers {

  import com.github.limansky.mongoquery.core.TestObjects._

  "Tepkin mq implementation" should "convert string into BSONDocument" in {
    val q = mq"{ amount : { $$lte : 15}}"
    q should equal($document("amount" := $document("$lte" := 15)))
  }

  it should "substitute primitive values in the query" in {
    val id = "15B-4"
    mq"{ orderId : $id }" should equal($document("orderId" := id))
  }

  it should "support nested objects" in {
    val q = mq"""{ user : "Joe", age : {$$gt : 25}}"""
    q should equal($document("user" := "Joe", "age" := $document("$gt" := 25)))
  }

  it should "substitute sequences as arrays in the query" in {
    val colors = List("red", "green", "blue")
    val q = mq"{ color : {$$in : $colors}}"
    q should equal($document("color" := $document("$in" := colors)))
  }

  it should "substitute dates" in {
    val now = new Date()
    mq"{start : {$$lte : $now}}" should be($document("start" := $document("$lte" := now)))
  }

  it should "support arrays of objects" in {
    val q = mq"""{ phones : [ { number : "223322", type : "home"}, { number: "332233", type: "work"} ] }"""
    q should equal($document("phones" := $array($document("number" := "223322", "type" := "home"), $document("number" := "332233", "type" := "work"))))
  }

  it should "support arrays of arrays" in {
    val q = mq"""{ indexes : [ [1,3,5], [4,6,7]] }"""
    q should equal($document("indexes" := $array($array(1, 3, 5), $array(4, 6, 7))))
  }

  it should "be possible to compose queries" in {
    val sub = mq"{$$gt : 10}"
    mq"{price : $sub}" should equal($document("price" := $document("$gt" := 10)))
  }

  it should "support BSONObjectIDs injection" in {
    val id = BsonObjectId.generate
    mq"{ clientId : $id }" should equal($document("clientId" := id))
  }

  /*  it should "support BsonObjectIds literals" in {
    val q = mq"""{ clientId : ObjectId("aabbccddeeff112233445566") }"""
    q should equal($document("clientId" := BsonObjectId("aabbccddeeff112233445566")))
  }*/

  it should "support boolean literals" in {
    mq"{bar : true}" should equal($document("bar" := true))
  }

  it should "support null" in {
    mq"{bar : null}" should equal($document("bar" := null))
  }

  it should "support empty objects" in {
    mq"{}" should equal($document())
  }

  "ReactiveHelper mqt implementation" should "pass valid object" in {
    mqt"{ s : 'test' }"[Foo] should equal($document("s" := "test"))
    mqt"{ i : 5, f.s : 'test' }"[Bar] should equal($document("i" := 5, "f.s" := "test"))
  }

  it should "ignore operators" in {
    mqt"{ $$set : { s : 'aaa'}}"[Foo] should equal($document("$set" := $document("s" := "aaa")))
  }

  it should "handle options" in {
    mqt"{ d : 3.14, f.s : 'bbb'}"[Baz] should equal($document("d" := 3.14, "f.s" := "bbb"))
  }

  it should "handle collections" in {
    mqt"{ s : 'ccc', lf.s : 'zzzz' }"[Quux] should equal($document("s" := "ccc", "lf.s" := "zzzz"))
    mqt"{ s : 'ccc', lf.1.s : 'zzzz' }"[Quux] should equal($document("s" := "ccc", "lf.1.s" := "zzzz"))
    mqt"{ s : 'ccc', lf.$$.s : 'zzzz' }"[Quux] should equal($document("s" := "ccc", "lf.$.s" := "zzzz"))
  }
}
