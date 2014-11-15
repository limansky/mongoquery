package com.github.limansky.mongoquery

import scala.language.experimental.macros
import com.mongodb.DBObject

package object casbah {

  implicit class CasbahQueryHelper(val sc: StringContext) extends AnyVal {
    def mq(args: Any*): DBObject = macro CasbahMacro.c_mqimpl
  }
}
