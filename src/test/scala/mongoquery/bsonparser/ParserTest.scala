package mongoquery.bsonparser

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ParserTest extends FlatSpec with Matchers {

  case class TestId(id: String)
  case class TestObject(data: Map[String, Any])

  object TestParser extends Parser[TestId, TestObject] {
    override def makeId(id: String) = TestId(id)

    override def makeObject(content: List[(String, Any)]) = TestObject(content.toMap)
  }

  def parseValue(s: String): Any = {
    TestParser.phrase(TestParser.value)(new TestParser.lexical.Scanner(s)) match {
      case TestParser.Success(r, _) => r
      case TestParser.NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  "BSON Parser" should "parse string values" in {
    parseValue("\"It's a string\"") should be("It's a string")
    parseValue("'a string'") should be("a string")
  }

  it should "parse int values" in {
    parseValue("42") should be(42)
  }

  it should "parse double values" in {
    parseValue("42.5") should be(42.5)
  }

  it should "parse objectId values" in {
    parseValue("ObjectId(\"0123456789abcdef01234567\")") should be(TestId("0123456789abcdef01234567"))
  }

  it should "not allow invalid ObjectIds" in {
    an[IllegalArgumentException] should be thrownBy (parseValue("ObjectId(\"Hello\")"))
  }

  it should "parse arrays" in {
    parseValue("[\"String\", 5, 3.14]") should be(
      List("String", 5, 3.14))
  }

  it should "parse objects" in {
    TestParser.parse("{a : 1, b : 2}") should be(
      TestObject(Map("a" -> 1, "b" -> 2)))
  }

  it should "parse nested arrays" in {
    TestParser.parse("{ c: [1,2,3]}") should be(
      TestObject(Map("c" -> List(1, 2, 3))))
  }

  it should "parse nested objects" in {
    TestParser.parse("{ d: { e : \"ooo\" }}") should be(
      TestObject(Map("d" -> TestObject(Map("e" -> "ooo")))))
  }

  it should "process special operators" in {
    TestParser.parse("{ $lt : 11 }") should be(TestObject(Map("$lt" -> 11)))
  }
}
