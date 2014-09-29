package mongoquery

sealed abstract class MongoValue

case class MongoString(s: String) extends MongoValue

case class MongoInt(i: Int) extends MongoValue

case class MongoDouble(d: Double) extends MongoValue

case class MongoArray(a: List[MongoValue]) extends MongoValue

case class MongoObject(vs: Map[String, MongoValue]) extends MongoValue
