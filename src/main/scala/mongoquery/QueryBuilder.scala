package mongoquery

abstract class QueryBuilder[T] {

  def build(query: String): T = {
    build(BSONParser.parse(query))
  }

  def build(obj: MongoObject): T = {

    def processValue(v: MongoValue[_]): Any = v match {
      case o: MongoObject => build(o)
      case a: MongoArray => a.value.map(processValue)
      case other => other.value
    }

    val values = obj.value.map(kv => (kv._1, processValue(kv._2))).toList

    boundValues(values)
  }

  def boundValues(values: List[(String,Any)]): T
}
