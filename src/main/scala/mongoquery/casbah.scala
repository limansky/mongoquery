package mongoquery

import com.mongodb.casbah.commons.MongoDBObject
import mongoquery.bsonparser.Parser

package object casbah {

  implicit class MongoQueryHelper(val sc: StringContext) extends AnyVal {

    def mq(args: Any*): MongoObject = {
      Parser.parse(sc.parts.toList, args.toList)
    }
  }

  import scala.language.implicitConversions

  implicit def mongoObjectToCasbah(obj: MongoObject): MongoDBObject = {
    CasbahQueryBuilder.build(obj)
  }
}
