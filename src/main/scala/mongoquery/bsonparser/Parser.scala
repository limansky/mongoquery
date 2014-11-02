package mongoquery.bsonparser

import scala.util.parsing.combinator.syntactical.StdTokenParsers
import mongoquery.MongoValue
import mongoquery.MongoString
import mongoquery.MongoInt
import mongoquery.MongoDouble
import mongoquery.MongoArray
import mongoquery.MongoObject

class Parser extends StdTokenParsers {

  override type Tokens = BSONTokens

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",")

  import lexical._

  def value: Parser[MongoValue[_]] = string | int | double | array | obj

  def string: Parser[MongoString] = elem("string", _.isInstanceOf[StringLit]) ^^ (v => MongoString(v.chars))

  def anyKeyword: Parser[String] = elem("string", _.isInstanceOf[Keyword]) ^^ (_.chars)

  def int: Parser[MongoInt] = elem("int", _.isInstanceOf[NumericLit]) ^^ (v => MongoInt(v.chars.toInt))

  def double: Parser[MongoDouble] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (v => MongoDouble(v.chars.toDouble))

  def array: Parser[MongoArray] = ("[" ~> repsep(value, ",") <~ "]") ^^ MongoArray

  def member: Parser[(String, MongoValue[_])] = (ident | anyKeyword) ~ ":" ~ value ^^ { case i ~ _ ~ v => (i, v) }

  def obj: Parser[MongoObject] = "{" ~> repsep(member, ",") <~ "}" ^^ { case kvs => MongoObject(kvs.toMap) }

}

object Parser extends Parser {
  def parse(expr: String): MongoObject = {
    phrase(obj)(new lexical.Scanner(expr)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }
}
