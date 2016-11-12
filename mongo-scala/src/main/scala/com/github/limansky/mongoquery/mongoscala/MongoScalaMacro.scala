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

package com.github.limansky.mongoquery.mongoscala

import com.github.limansky.mongoquery.core.MacroContext.Context
import com.github.limansky.mongoquery.core.MongoQueryMacro
import org.mongodb.scala.bson.Document

/**
 * Macro implementation for ReactiveMongo.
 */
object MongoScalaMacro extends MongoQueryMacro {

  type DBType = Document

  def r_mq_impl(c: Context)(args: c.Expr[Any]*): c.Expr[Document] = mq_impl(c)(args: _*)

  def r_mqt_impl[T: c.WeakTypeTag](c: Context): c.Expr[Document] = mqt_impl[T](c)

  override def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[Document] = {
    import c.universe._
    c.Expr(q"org.mongodb.scala.bson.Document(..$dbparts)")
  }

  override def createId(c: Context)(id: String): c.Expr[Any] = {
    import c.universe._
    c.Expr(q"org.mongodb.scala.bson.BsonObjectId($id)")
  }

  override def createRegex(c: Context)(expression: String, options: String): c.Expr[Any] = {
    import c.universe._

    c.Expr(q"org.mongodb.scala.bson.BsonRegularExpression($expression, $options)")
  }

  override def createNull(c: Context): c.Expr[Any] = {
    import c.universe._

    c.Expr(q"org.mongodb.scala.bson.BsonNull()")
  }
}
