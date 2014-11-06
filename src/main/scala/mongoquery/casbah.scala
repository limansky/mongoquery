package mongoquery

import com.mongodb.casbah.commons.MongoDBObject
import mongoquery.bsonparser.Parser

package object casbah {

  implicit class MongoQueryHelper(val sc: StringContext) extends AnyVal {

    def mq(args: Any*) = {
      CasbahParser.parse(sc.parts.toList, args.toList)
    }
  }
}
