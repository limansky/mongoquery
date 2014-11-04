package mongoquery.bsonparser

import scala.util.parsing.combinator.syntactical.StdTokenParsers
import mongoquery.MongoValue
import mongoquery.MongoString
import mongoquery.MongoInt
import mongoquery.MongoDouble
import mongoquery.MongoArray
import mongoquery.MongoObject
import scala.util.parsing.input.CharArrayReader
import mongoquery.MongoArray

class Parser extends StdTokenParsers {

  override type Tokens = BSONTokens

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",")

  import lexical._

  def value: Parser[MongoValue[_]] = string | int | double | array | obj

  def string: Parser[MongoString] = elem("string", _.isInstanceOf[StringLit]) ^^ (v => MongoString(v.chars))

  def anyKeyword: Parser[String] = elem("keyword", _.isInstanceOf[Keyword]) ^^ (_.chars)

  def variable: Parser[MongoValue[_]] = elem("var", _.isInstanceOf[Variable]) ^^ (
    v => wrapVariable(v.asInstanceOf[Variable].v))

  def int: Parser[MongoInt] = elem("int", _.isInstanceOf[NumericLit]) ^^ (v => MongoInt(v.chars.toInt))

  def double: Parser[MongoDouble] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (v => MongoDouble(v.chars.toDouble))

  def array: Parser[MongoArray] = ("[" ~> repsep(value, ",") <~ "]") ^^ MongoArray

  def member: Parser[(String, MongoValue[_])] = (ident | anyKeyword) ~ ":" ~ (value | variable) ^^ {
    case i ~ _ ~ v => (i, v)
  }

  def obj: Parser[MongoObject] = "{" ~> repsep(member, ",") <~ "}" ^^ { case kvs => MongoObject(kvs.toMap) }

  def wrapVariable(v: Any): MongoValue[_] = v match {
    case s: String => MongoString(s)
    case i: Int => MongoInt(i)
    case d: Double => MongoDouble(d)
    case s: Traversable[_] => MongoArray(s.map(wrapVariable).toList)
    case m: MongoValue[_] => m
  }
}

object Parser extends Parser {
  def parse(expr: String): MongoObject = {
    phrase(obj)(new lexical.Scanner(expr)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  def parse(parts: List[String], args: Seq[Any]): MongoObject = {
    val rs = parts.map(p => new CharArrayReader(p.toCharArray))
    phrase(obj)(new lexical.Scanner(rs, args)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }
}
