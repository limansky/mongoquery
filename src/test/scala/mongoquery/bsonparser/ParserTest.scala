package mongoquery.bsonparser

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import mongoquery._

class ParserTest extends FlatSpec with Matchers {

  def parseValue(s: String): MongoValue[_] = {
    Parser.phrase(Parser.value)(new Parser.lexical.Scanner(s)) match {
      case Parser.Success(r, _) => r
      case Parser.NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  "BSON Parser" should "parse string values" in {
    parseValue("\"It's a string\"") should be(MongoString("It's a string"))
    parseValue("'a string'") should be(MongoString("a string"))
  }

  it should "parse int values" in {
    parseValue("42") should be(MongoInt(42))
  }

  it should "parse double values" in {
    parseValue("42.5") should be(MongoDouble(42.5))
  }

  it should "parse objectId values" in {
    parseValue("ObjectId(\"0123456789abcdef01234567\")") should be(MongoId("0123456789abcdef01234567"))
  }

  it should "not allow invalid ObjectIds" in {
    an[IllegalArgumentException] should be thrownBy (parseValue("ObjectId(\"Hello\")"))
  }

  it should "parse arrays" in {
    parseValue("[\"String\", 5, 3.14]") should be(
      MongoArray(List(MongoString("String"), MongoInt(5), MongoDouble(3.14))))
  }

  it should "parse objects" in {
    Parser.parse("{a : 1, b : 2}") should be(
      MongoObject(Map("a" -> MongoInt(1), "b" -> MongoInt(2))))
  }

  it should "parse nested arrays" in {
    Parser.parse("{ c: [1,2,3]}") should be(
      MongoObject(Map("c" -> MongoArray(List(MongoInt(1), MongoInt(2), MongoInt(3))))))
  }

  it should "parse nested objects" in {
    Parser.parse("{ d: { e : \"ooo\" }}") should be(
      MongoObject(Map("d" -> MongoObject(Map("e" -> MongoString("ooo"))))))
  }

  it should "process special operators" in {
    Parser.parse("{ $lt : 11 }") should be(MongoObject(Map("$lt" -> MongoInt(11))))
  }
}
