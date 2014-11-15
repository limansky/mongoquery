package com.github.limansky.mongoquery.casbah

import com.github.limansky.mongoquery.core.MongoQueryMacro
import com.github.limansky.mongoquery.core.MacroContext.Context
import com.mongodb.DBObject

object CasbahMacro extends MongoQueryMacro {

  type DBType = DBObject
  type Parser = CasbahParser

  override object parser extends CasbahParser

  def c_mqimpl(c: Context)(args: c.Expr[Any]*): c.Expr[DBObject] = mqimpl(c)(args: _*)

  override def createObject(c: Context)(dbparts: List[(String, c.Expr[Any])]): c.Expr[DBObject] = {
    import c.universe._

    c.Expr(q"com.mongodb.casbah.commons.MongoDBObject(..$dbparts)")
  }
}
