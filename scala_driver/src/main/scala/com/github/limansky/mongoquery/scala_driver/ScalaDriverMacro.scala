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

package com.github.limansky.mongoquery.scala_driver

import com.github.limansky.mongoquery.core.MacroContext.Context
import com.github.limansky.mongoquery.core.MongoQueryMacro
import org.mongodb.scala.bson.BsonDocument
import scala.language.experimental.macros

/**
 * Macro implementation for Mogno Scala Driver.
 */
object ScalaDriverMacro extends MongoQueryMacro {

  type DBType = BsonDocument

  def r_mq_impl(c: Context)(args: c.Expr[Any]*): c.Expr[BsonDocument] = mq_impl(c)(args: _*)

  def r_mqt_impl[T: c.WeakTypeTag](c: Context): c.Expr[BsonDocument] = mqt_impl[T](c)

  override def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[BsonDocument] = {
    import c.universe._
    c.Expr(q"org.mongodb.scala.bson.BsonDocument(..$dbparts)")
  }

  override def createId(c: Context)(id: String): c.Expr[Any] = {
    import c.universe._
    c.Expr(q"org.mongodb.scala.bson.BsonObjectId($id)")
  }

  override def createRegex(c: Context)(expression: String, options: String): c.Expr[Any] = {
    import c.universe._

    c.Expr(q"org.mongodb.scala.bson.BsonRegularExpression($expression, $options)")
  }
}
