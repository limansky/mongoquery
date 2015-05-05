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

import com.github.limansky.mongoquery.core.MongoQueryMacro
import com.github.limansky.mongoquery.core.MacroContext.Context
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.util.Converters

/**
 * Macro implementation for Tepkin.
 */
object TepkinMacro extends MongoQueryMacro {

  type DBType = BsonDocument

  def t_mq_impl(c: Context)(args: c.Expr[Any]*): c.Expr[BsonDocument] = mq_impl(c)(args: _*)

  def t_mqt_impl[T: c.WeakTypeTag](c: Context): c.Expr[BsonDocument] = mqt_impl[T](c)

  override def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[BsonDocument] = {
    import c.universe._
    c.Expr(q"net.fehmicansaglam.bson.BsonDocument.from($dbparts)")
  }

  override def createId(c: Context)(id: String): c.Expr[Any] = {
    import c.universe._
    val hex = Converters.str2Hex(id)
    c.Expr(q"new net.fehmicansaglam.bson.Implicits.BsonValueObjectId($hex)")
  }
}
