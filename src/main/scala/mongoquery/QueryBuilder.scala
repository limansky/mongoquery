package mongoquery

abstract class QueryBuilder[T] {

  def build(query: String): T = {
    build(BSONParser.parse(query))
  }

  def build(obj: MongoObject): T = {
    val values = obj.value.map {
      case (key, obj: MongoObject) => (key, build(obj))
      case (key, value) => (key, value.value)
    }.toList

    boundValues(values)
  }

  def boundValues(values: List[(String,Any)]): T
}