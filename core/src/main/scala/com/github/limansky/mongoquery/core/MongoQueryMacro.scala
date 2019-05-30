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

import com.github.limansky.mongoquery.core.BSON.{ LValue, Member }
import com.github.limansky.mongoquery.core.MacroContext.Context
import com.github.limansky.mongoquery.core.bsonparser.Parser

/**
 * Base macro implemenation without dependency to any MongoDB driver.
 *
 * If you going to create MongoQuery implementation for your driver it a nice
 * place to start to extend this trait.
 */
trait MongoQueryMacro {

  /**
   * Type MongoDB documents representation.
   */
  type DBType

  /**
   * Creates DB object from the List of parsed data.
   *
   * @param c Scala macro context
   * @param dbParts list of key/value pairs. The first parameter is a name of
   * an object field, and the second one is an expression to be used as a value.
   *
   * @return created object.
   */
  def createObject(c: Context)(dbParts: List[(String, c.Expr[Any])]): c.Expr[DBType]

  /**
   * Creates id value.
   *
   * @param c Scala macro context
   *
   * @param id 40 character hex number represented as a string.  The string is
   * already validated, so it only required to wrap it into DB Id
   * representation.
   *
   * @return created id.
   */
  def createId(c: Context)(id: String): c.Expr[Any]

  /**
   * Creates regex literal.
   *
   * @param c Scala macro context
   *
   * @param expression regular expression to be wrapped.
   * @param options options for this expression.
   *
   * @return created regex.
   */
  def createRegex(c: Context)(expression: String, options: String): c.Expr[Any]

  def createNull(c: Context): c.Expr[Any]

  /**
   * This is mq interpolator entry point.
   */
  def mq_impl(c: Context)(args: c.Expr[Any]*): c.Expr[DBType] = {
    import c.universe._

    val Apply(_, List(Apply(_, partsTrees))) = c.prefix.tree

    val parsed = parse(c)(partsTrees)
    wrapObject(c)(parsed.members, args.iterator)
  }

  /**
   * This is mqt interpolator entry point.
   */
  def mqt_impl[T: c.WeakTypeTag](c: Context): c.Expr[DBType] = {
    import c.universe._

    val analyzer = new TypeInfoAnalyzer[c.type](c)

    val q"$cn(scala.StringContext.apply(..$partsTrees)).mqt(..$argsTrees)" = c.prefix.tree
    val args = argsTrees.map(c.Expr(_))
    val parsed = parse(c)(partsTrees)

    val errors = parsed.members.foldLeft(List.empty[String]) {
      case (e, p) =>
        p match {
          case (m: Member, v) =>
            analyzer.check(c.weakTypeOf[T])((m, v)) match {
              case Left(msg) => msg :: e
              case _ => e
            }
          case _ => e
        }
    }

    if (errors.nonEmpty) {
      c.abort(c.enclosingPosition, errors.mkString(","))
    }

    wrapObject(c)(parsed.members, args.iterator)
  }

  /**
   * Parses parts provided by interpolator context.
   *
   * @param c macro context
   * @param partsTrees string parts to be parsed. The data is provided as a
   * parts seprarated by substitutions.  For example:
   *
   * {{{
   * val name = "John Doe"
   * val age = 42
   * mq"{ name : $$name, age: $$age }"
   * }}}
   *
   * In this example parts will be: "{ name: ", ", age: " and " }"
   */
  protected def parse(c: Context)(partsTrees: List[c.Tree]): BSON.Object = {
    import c.universe._

    val parts = partsTrees map { case Literal(Constant(s: String)) => s }

    val parser = new Parser

    parser.parse(parts) match {
      case parser.Success(obj, _) => obj

      case parser.NoSuccess(msg, r) =>
        val partIndex = r match {
          case scanner: parser.lexical.Scanner =>
            scanner.part
          case _ =>
            0
        }
        val pos = partsTrees(partIndex).pos
        c.abort(pos.withPoint(pos.point + r.offset), msg)
    }
  }

  /**
   * Wraps values.
   *
   * This method is required to insert arguments between parts, wrap inlined
   * ids, nested objects and lists.
   */
  protected def wrapValue(c: Context)(value: Any, args: Iterator[c.Expr[_]]): c.Expr[Any] = {
    import c.universe._
    value match {
      case BSON.Placeholder => c.Expr(args.next().tree)
      case BSON.Object(m) => wrapObject(c)(m, args)
      case BSON.Id(id) => createId(c)(id)
      case BSON.Regex(r, opt) => createRegex(c)(r, opt)
      case BSON.NullObj => createNull(c)
      case a: List[_] =>
        val wrapped = a.map(i => wrapValue(c)(i, args))
        c.Expr[List[Any]](q"List(..$wrapped)")
      case v => c.Expr[Any](Literal(Constant(v)))
    }
  }

  /**
   * Wraps object into DBType.
   */
  protected def wrapObject(c: Context)(parts: List[(LValue, Any)], args: Iterator[c.Expr[_]]): c.Expr[DBType] = {
    val dbParts = parts.map {
      case (lv, v) => (lv.asString, wrapValue(c)(v, args))
    }

    createObject(c)(dbParts)
  }
}
