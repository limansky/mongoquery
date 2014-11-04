package mongoquery

import mongoquery.bsonparser.Parser

abstract class QueryBuilder[T] {

  def build(obj: MongoObject): T = {

    def processValue(v: MongoValue[_]): Any = v match {
      case o: MongoObject => build(o)
      case a: MongoArray => a.value.map(processValue)
      case other => other.value
    }

    val values = obj.value.map(kv => (kv._1, processValue(kv._2))).toList

    boundValues(values)
  }

  def boundValues(values: List[(String, Any)]): T
}
