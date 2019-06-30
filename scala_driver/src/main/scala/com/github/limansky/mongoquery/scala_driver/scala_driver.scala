package com.github.limansky.mongoquery

import scala.language.experimental.macros
import org.mongodb.scala.bson.BsonDocument

package object scala_driver {

  class QueryWrapper {
    def apply[T]: BsonDocument = macro ScalaDriverMacro.r_mqt_impl[T]
  }

  implicit class ReactiveQueryHelper(val sc: StringContext) extends AnyVal {
    def mq(args: Any*): BsonDocument = macro ScalaDriverMacro.r_mq_impl

    def mqt(args: Any*) = new QueryWrapper
  }
}
