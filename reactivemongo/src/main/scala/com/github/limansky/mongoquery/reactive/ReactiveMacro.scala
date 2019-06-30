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

import com.github.limansky.mongoquery.core.MongoQueryMacro
import reactivemongo.bson.BSONDocument

import scala.reflect.macros.blackbox

/**
 * Macro implementation for ReactiveMongo.
 */
object ReactiveMacro extends MongoQueryMacro {

  type DBType = BSONDocument

  def r_mq_impl(c: blackbox.Context)(args: c.Expr[Any]*): c.Expr[BSONDocument] = mq_impl(c)(args: _*)

  def r_mqt_impl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[BSONDocument] = mqt_impl[T](c)

  override def createObject(c: blackbox.Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[BSONDocument] = {
    import c.universe._
    c.Expr(q"reactivemongo.bson.BSONDocument(..$dbparts)")
  }

  override def createId(c: blackbox.Context)(id: String): c.Expr[Any] = {
    import c.universe._
    c.Expr(q"reactivemongo.bson.BSONObjectID.parse($id).get")
  }

  override def createRegex(c: blackbox.Context)(expression: String, options: String): c.Expr[Any] = {
    import c.universe._

    c.Expr(q"reactivemongo.bson.BSONRegex($expression, $options)")
  }

  override def createNull(c: blackbox.Context): c.Expr[Any] = {
    import c.universe._

    c.Expr(q"reactivemongo.bson.BSONNull")
  }
}
