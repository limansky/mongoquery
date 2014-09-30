package mongoquery

import com.mongodb.casbah.Imports._

class CasbahQueryBuilder extends QueryBuilder[MongoDBObject] {

  override def boundValues(values: List[(String,Any)]): MongoDBObject = {
    MongoDBObject(values)
  }
}