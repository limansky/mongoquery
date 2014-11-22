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

package com.github.limansky.mongoquery.core.bsonparser

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ParserTest extends FlatSpec with Matchers {

  object TestParser extends Parser

  import TestParser.Object

  def parseValue(s: String): Any = {
    TestParser.phrase(TestParser.value)(new TestParser.lexical.Scanner(s)) match {
      case TestParser.Success(r, _) => r
      case TestParser.NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  "BSON Parser" should "parse string values" in {
    parseValue("\"It's a string\"") should be("It's a string")
    parseValue("'a string'") should be("a string")
  }

  it should "parse int values" in {
    parseValue("42") should be(42)
  }

  it should "support negative integers" in {
    parseValue("-5") should be(-5)
  }

  it should "parse double values" in {
    parseValue("42.5") should be(42.5)
  }

  it should "parse negative doubles" in {
    parseValue("-8.33") should be(-8.33)
  }

  it should "parse objectId values" in {
    parseValue("ObjectId(\"0123456789abcdef01234567\")") should be(TestParser.Id("0123456789abcdef01234567"))
  }

  it should "not allow invalid ObjectIds" in {
    an[IllegalArgumentException] should be thrownBy (parseValue("ObjectId(\"Hello\")"))
  }

  it should "parse arrays" in {
    parseValue("[\"String\", 5, 3.14]") should be(
      List("String", 5, 3.14))
  }

  it should "parse objects" in {
    TestParser.parse("{a : 1, b : 2}") should be(
      Object(List("a" -> 1, "b" -> 2)))
  }

  it should "parse nested arrays" in {
    TestParser.parse("{ c: [1,2,3]}") should be(
      Object(List("c" -> List(1, 2, 3))))
  }

  it should "parse nested objects" in {
    TestParser.parse("{ d: { e : \"ooo\" }}") should be(
      Object(List("d" -> Object(List("e" -> "ooo")))))
  }

  it should "process special operators" in {
    TestParser.parse("{ $lt : 11 }") should be(Object(List("$lt" -> 11)))
  }

  it should "allow access to inner fields" in {
    TestParser.parse("{ employee.name : \"John\" }") should be(Object(List("employee.name" -> "John")))
    TestParser.parse("{ user.address.building : \"10\" }") should be(Object(List("user.address.building" -> "10")))
  }

  it should "allow access to array item" in {
    TestParser.parse("{ clients.3 : 5 }") should be(Object(List("clients.3" -> 5)))
    TestParser.parse("{ clients.$.size : { $gt : 100 }}") should be(Object(List("clients.$.size" -> Object(List("$gt" -> 100)))))
  }

  it should "be possible to use boolean literals" in {
    TestParser.parse("{ foo: true, bar: false }") should be(Object(List("foo" -> true, "bar" -> false)))
  }
}
