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

package com.github.limansky.mongoquery.core

import com.github.limansky.mongoquery.core.BSON.LValue
import com.github.limansky.mongoquery.core.bsonparser.Parser

trait MongoQueryParser {
  type DbType

  protected def createObject(dbParts: List[(String, Any)]): DbType
  protected def createRegex(r: String, opt: String): Any
  protected def createId(id: String): Any
  protected def createNull: Any

  def parse(bson: String): DbType = {
    val parser = new Parser

    parser.parse(bson) match {
      case parser.Success(obj, _) =>
        wrapObject(obj.members)
      case parser.NoSuccess(msg, _) => throw new IllegalArgumentException(msg)
    }
  }

  def wrapValue(value: Any): Any = {
    value match {
      case BSON.Object(m) => wrapObject(m)
      case BSON.Id(id) => createId(id)
      case BSON.Regex(r, opt) => createRegex(r, opt)
      case BSON.NullObj => createNull
      case list: List[_] => list.map(wrapValue)
      case v => v
    }
  }

  def wrapObject(parts: List[(LValue, Any)]): DbType = {
    val dbParts = parts map {
      case (lv, v) => (lv.asString, wrapValue(v))
    }

    createObject(dbParts)
  }
}
