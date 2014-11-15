package com.github.limansky.mongoquery

import com.mongodb.casbah.commons.MongoDBObject
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import com.mongodb.DBObject
import org.bson.types.ObjectId

package object casbah {

  import casbah.CasbahParser._

  implicit class MongoQueryHelper(val sc: StringContext) extends AnyVal {
    def mq(args: Any*): DBObject = macro mqimpl
  }

  def mqimpl(c: Context)(args: c.Expr[Any]*): c.Expr[DBObject] = {
    import c.universe._

    lazy val a = args.iterator

    def wrapObject(parts: List[(String, Any)]): c.Expr[DBObject] = {
      val dbparts = parts.map {
        case (i, v) => (i, wrapValue(v))
      }

      c.Expr[DBObject](q"com.mongodb.casbah.commons.MongoDBObject(..$dbparts)")
    }

    def wrapValue(value: Any): c.Expr[Any] = value match {
      case Placeholder => a.next()
      case Object(m) => wrapObject(m)
      case a: List[_] =>
        val wrapped = a.map(i => wrapValue(i))
        c.Expr[List[Any]](q"List(..$wrapped)")
      case v => c.Expr[Any](Literal(Constant(v)))
    }

    val Apply(_, List(Apply(_, partsTrees))) = c.prefix.tree
    val parts = partsTrees map { case Literal(Constant(s: String)) => s }

    val parsed = try {
      CasbahParser.parse(parts)
    } catch {
      case e: IllegalArgumentException => c.abort(c.enclosingPosition, e.getMessage)
    }

    wrapObject(parsed.membes)
  }
}
