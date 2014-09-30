package mongoquery

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BSONParserTest extends FlatSpec with Matchers {

  import BSONParser._

  def parse[T](p: Parser[T], s: String): Option[T] = {
    BSONParser.parseAll(p, s) match {
      case Success(r, _) => Some(r)
      case NoSuccess(m, _) =>
        sys.error(m)
        None
    }
  }

  "BSONParser" should "parse string values" in {
    parse(BSONParser.value, "\"It's a string\"") should be (Some(MongoString("It's a string")))
    parse(BSONParser.value, "'a string'") should be (Some(MongoString("a string")))
  }

  it should "parse int values" in {
    parse(BSONParser.value, "42") should be (Some(MongoInt(42)))
  }

  it should "parse double values" in {
    parse(BSONParser.value, "42.5") should be (Some(MongoDouble(42.5)))
  }

  it should "parse arrays" in {
    parse(BSONParser.value, "[\"String\", 5, 3.14]") should be (Some(
        MongoArray(List(MongoString("String"), MongoInt(5), MongoDouble(3.14)))))
  }

  it should "parse key value pairs" in {
    parse(BSONParser.member, "key : 'value'") should be (Some(("key", MongoString("value"))))
    parse(BSONParser.member, "\"key\" : 33") should be (Some(("key", MongoInt(33))))
  }

  it should "parse objects" in {
    parse(BSONParser.obj, "{a : 1, b : 2}") should be (Some(
        MongoObject(Map("a" -> MongoInt(1), "b" -> MongoInt(2)))))
  }

  it should "parse nested arrays" in {
    parse(BSONParser.obj, "{ c: [1,2,3]}") should be (Some(
        MongoObject(Map("c" -> MongoArray(List(MongoInt(1), MongoInt(2), MongoInt(3)))))))
  }

  it should "parse nested objects" in {
    parse(BSONParser.obj, "{ d: { e : \"ooo\" }}") should be (Some(
        MongoObject(Map("d" -> MongoObject(Map("e" -> MongoString("ooo")))))))
  }

  it should "process special operators" in {
    parse(BSONParser.obj, "{ $lt : 11 }") should be (Some(MongoObject(Map("$lt" -> MongoInt(11)))))
  }
}
