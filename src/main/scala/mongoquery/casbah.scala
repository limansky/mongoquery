package mongoquery

import com.mongodb.casbah.commons.MongoDBObject

package object casbah {

  implicit class CasbahHelper(val sc: StringContext) extends AnyVal {

    def mq(args: Any*): MongoDBObject = {
      val str = sc.s(args.map(wrapValue): _*)
      CasbahQueryBuilder.build(str)
    }

    def wrapValue(v: Any): String = v match {
      case a: Traversable[_] => a.map(wrapValue).mkString("[", ",", "]")
      case s: String => '"' + s + '"'
      case other => other.toString()
    }
  }
}
