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

import com.github.limansky.mongoquery.core.MongoQueryParser
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId

object BSONParser extends MongoQueryParser {
  type DbType = DBObject

  override def createObject(dbParts: List[(String, Any)]): DBObject = MongoDBObject(dbParts)

  override def createRegex(expression: String, options: String): Any = expression.r

  override def createId(id: String): Any = new ObjectId(id)
}
