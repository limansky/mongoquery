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

package com.github.limansky.mongoquery.scala_driver

import org.mongodb.scala.bson._
import com.github.limansky.mongoquery.core.BSON
import com.github.limansky.mongoquery.core.bsonparser.Parser
import com.github.limansky.mongoquery.core.BSON.LValue

object BSONParser {
  def parse(bson: String): BsonDocument = {
    val parser = new Parser

    parser.parse(bson) match {
      case parser.Success(obj, _) =>
        wrapObject(obj.members)
      case parser.NoSuccess(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def wrapValue(value: Any): BsonValue = {
    value match {
      case BSON.Object(m) => wrapObject(m)
      case BSON.Id(id) => BsonObjectId(id) // .. check id correctness
      case BSON.Regex(r, opt) => BsonRegularExpression(r, opt)
      case list: List[_] => BsonArray(list.map(wrapValue))
      case s: String => BsonString(s)
      case n: Double => BsonDouble(n)
      case i: Int => BsonInt32(i)
      case l: Long => BsonInt64(l)
      case b: Boolean => BsonBoolean(b)
      case null => BsonNull()
      case x => throw new IllegalStateException(s"Unsupported value $x")
    }
  }

  def wrapObject(parts: List[(LValue, Any)]): BsonDocument = {
    val dbParts = parts map {
      case (lv, v) => (lv.asString, wrapValue(v))
    }

    BsonDocument(dbParts)
  }
}
