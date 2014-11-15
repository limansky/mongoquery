package com.github.limansky.mongoquery.casbah

import com.github.limansky.mongoquery.core.bsonparser.Parser
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.DBObject

trait CasbahParser extends Parser[ObjectId] {
  override def makeId(id: String) = new ObjectId(id)
}
