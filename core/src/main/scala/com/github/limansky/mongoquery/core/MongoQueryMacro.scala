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
import scala.language.experimental.macros
import com.github.limansky.mongoquery.core.BSON.Member
import com.github.limansky.mongoquery.core.BSON.LValue

trait MongoQueryMacro {

  type DBType

  def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[DBType]
  def createId(c: Context)(id: String): c.Expr[Any]

  def mq_impl(c: Context)(args: c.Expr[Any]*): c.Expr[DBType] = {
    import c.universe._

    val Apply(_, List(Apply(_, partsTrees))) = c.prefix.tree

    val parsed = parse(c)(partsTrees, v => Right(v))
    wrapObject(c)(parsed.members, args.map(_.tree).iterator)
  }

  def mqt_impl[T: c.WeakTypeTag](c: Context): c.Expr[DBType] = {
    import c.universe._

    val analyzer = new TypeInfoAnalyzer[c.type](c)

    val Apply(Select(Apply(_, List(Apply(_, partsTrees))), _), argsTrees) = c.prefix.tree
    val parsed = parse(c)(partsTrees, analyzer.check(c.weakTypeOf[T]))

    wrapObject(c)(parsed.members, argsTrees.iterator)
  }

  private def parse(c: Context)(partsTrees: List[c.Tree], check: Function1[(Member, Any), Either[String, (Member, Any)]]) = {
    import c.universe._

    val parts = partsTrees map { case Literal(Constant(s: String)) => s }
    val positions = partsTrees.map(_.pos)

    val parser = new Parser(check)

    parser.parse(parts) match {
      case parser.Success(obj, _) => obj
      case parser.NoSuccess(msg, r) =>
        val partIndex = if (r.isInstanceOf[parser.lexical.Scanner]) {
          r.asInstanceOf[parser.lexical.Scanner].part
        } else {
          0
        }
        val part = positions(partIndex)
        c.abort(part.withPoint(part.point + r.offset), msg)
    }
  }

  private def wrapValue(c: Context)(value: Any, args: Iterator[c.Tree]): c.Expr[Any] = {
    import c.universe._
    value match {
      case BSON.Placeholder => c.Expr(args.next())
      case BSON.Object(m) => wrapObject(c)(m, args)
      case BSON.Id(id) => createId(c)(id)
      case a: List[_] =>
        val wrapped = a.map(i => wrapValue(c)(i, args))
        c.Expr[List[Any]](q"List(..$wrapped)")
      case v => c.Expr[Any](Literal(Constant(v)))
    }
  }

  private def wrapObject(c: Context)(parts: List[(LValue, Any)], args: Iterator[c.Tree]): c.Expr[DBType] = {
    val dbparts = parts.map {
      case (lv, v) => (lv.asString, wrapValue(c)(v, args))
    }

    createObject(c)(dbparts)
  }
}
