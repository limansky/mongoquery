package mongoquery

sealed abstract class MongoValue[T](val value: T)

case class MongoString(override val value: String) extends MongoValue[String](value)

case class MongoInt(override val value: Int) extends MongoValue[Int](value)

case class MongoDouble(override val value: Double) extends MongoValue[Double](value)

case class MongoArray(override val value: List[MongoValue[_]]) extends MongoValue[List[MongoValue[_]]](value)

case class MongoObject(override val value: Map[String, MongoValue[_]]) extends MongoValue[Map[String, MongoValue[_]]](value)
