package mongoquery

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BSONParserTest extends FlatSpec with Matchers {

  import BSONParser._

  def parse[T](p: Parser[T], s: String): Option[T] = {
    BSONParser.parseAll(p, s) match {
      case Success(r, _) => Some(r)
      case _ => None
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

  it should "parse key value pairs" in {
    parse(BSONParser.member, "key : 'value'") should be (Some(("key", MongoString("value"))))
    parse(BSONParser.member, "\"key\" : 33") should be (Some(("key", MongoInt(33))))
  }

  it should "parse objects" in {
    parse(BSONParser.obj, "{a : 1, b : 2}") should be (Some(MongoObject(Map("a" -> MongoInt(1), "b" -> MongoInt(2)))))
  }

}