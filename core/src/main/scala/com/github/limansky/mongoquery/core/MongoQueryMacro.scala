package com.github.limansky.mongoquery.core

import MacroContext.Context

trait MongoQueryMacro {

  protected def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[DBType]

  type DBType
  type Parser <: bsonparser.Parser[_]

  val parser: Parser

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

    wrapObject(parsed.membes)
  }
}
