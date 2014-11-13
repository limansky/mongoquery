package mongoquery

import mongoquery.bsonparser.Parser
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.DBObject

object CasbahParser extends Parser[ObjectId] {

  override def makeId(id: String) = new ObjectId(id)
}
