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

package com.github.limansky.mongoquery.core

import MacroContext.Context
import bsonparser.Parser

trait MongoQueryMacro {

  protected def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[DBType]

  protected def createId(c: Context)(id: String): c.Expr[Any]

  type DBType

  object parser extends Parser

  def mqimpl(c: Context)(args: c.Expr[Any]*): c.Expr[DBType] = {
    import c.universe._

    lazy val a = args.iterator

    def wrapObject(parts: List[(String, Any)]): c.Expr[DBType] = {
      val dbparts = parts.map {
        case (i, v) => (i, wrapValue(v))
      }

      createObject(c)(dbparts)
    }

    def wrapValue(value: Any): c.Expr[Any] = value match {
      case parser.Placeholder => a.next()
      case parser.Object(m) => wrapObject(m)
      case parser.Id(id) => createId(c)(id)
      case a: List[_] =>
        val wrapped = a.map(i => wrapValue(i))
        c.Expr[List[Any]](q"List(..$wrapped)")
      case v => c.Expr[Any](Literal(Constant(v)))
    }

    val Apply(_, List(Apply(_, partsTrees))) = c.prefix.tree
    val parts = partsTrees map { case Literal(Constant(s: String)) => s }

    val parsed = try {
      parser.parse(parts)
    } catch {
      case e: IllegalArgumentException => c.abort(c.enclosingPosition, e.getMessage)
    }

    wrapObject(parsed.members)
  }
}
