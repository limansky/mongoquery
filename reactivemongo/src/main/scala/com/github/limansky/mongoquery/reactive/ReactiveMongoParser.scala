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

package com.github.limansky.mongoquery.reactive

import com.github.limansky.mongoquery.core.BSON.LValue
import com.github.limansky.mongoquery.core.BSON
import com.github.limansky.mongoquery.core.bsonparser.Parser
import reactivemongo.bson._

object ReactiveMongoParser {
  def parse(bson: String): BSONDocument = {
    val parser = new Parser

    parser.parse(bson) match {
      case parser.Success(obj, _) =>
        wrapObject(obj.members)
      case parser.NoSuccess(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def wrapValue(value: Any): BSONValue = {
    value match {
      case BSON.Object(m) => wrapObject(m)
      case BSON.Id(id) => BSONObjectID(id)
      case BSON.Regex(r, opt) => BSONRegex(r, opt)
      case list: List[_] => BSONArray(list.map(wrapValue))
      case s: String => BSONString(s)
      case n: Double => BSONDouble(n)
      case b: Boolean => BSONBoolean(b)
      case x => throw new IllegalStateException(s"Unsupported value $x")
    }
  }

  def wrapObject(parts: List[(LValue, Any)]): BSONDocument = {
    val dbParts = parts map {
      case (lv, v) => (lv.asString, wrapValue(v))
    }

    BSONDocument(dbParts)
  }
}
