package mongoquery.bsonparser

import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.input.CharArrayReader

trait Parser[IdType, ObjectType] extends StdTokenParsers {

  override type Tokens = BSONTokens

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",", "(", ")")
  lexical.reserved ++= List("ObjectId")

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray

  import lexical._

  def makeId(id: String): IdType

  def makeObject(content: List[(String, Any)]): ObjectType

  def value: Parser[Any] = id | stringLit | int | double | array | obj

  def anyKeyword: Parser[String] = elem("keyword", _.isInstanceOf[Keyword]) ^^ (_.chars)

  def variable: Parser[Any] = elem("var", _.isInstanceOf[Variable]) ^^ (
    v => v.asInstanceOf[Variable].v)

  def int: Parser[Int] = numericLit ^^ (_.toInt)

  def double: Parser[Double] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (_.chars.toDouble)

  def objectIdValue = acceptIf(t => t.isInstanceOf[StringLit] && t.chars.length() == 24 && t.chars.forall(hexDigits.contains))(t => "Invalid object id: " + t.chars) ^^ (v => v.chars)

  def id: Parser[IdType] = keyword("ObjectId") ~> "(" ~> objectIdValue <~ ")" ^^ makeId

  def array: Parser[List[Any]] = ("[" ~> repsep(value, ",") <~ "]")

  def member: Parser[(String, Any)] = (ident | anyKeyword) ~ ":" ~ (value | variable) ^^ {
    case i ~ _ ~ v => (i, v)
  }

  def obj: Parser[ObjectType] = "{" ~> repsep(member, ",") <~ "}" ^^ makeObject

  def parse(expr: String): ObjectType = {
    phrase(obj)(new lexical.Scanner(expr)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  def parse(parts: List[String], args: Seq[Any]): ObjectType = {
    val rs = parts.map(p => new CharArrayReader(p.toCharArray))
    phrase(obj)(new lexical.Scanner(rs, args)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }
}
