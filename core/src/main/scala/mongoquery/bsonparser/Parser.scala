package com.github.limansky.mongoquery.core.bsonparser

import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.input.CharArrayReader

trait Parser[IdType] extends StdTokenParsers {

  override type Tokens = BSONTokens

  case object Placeholder
  case class Object(membes: List[(String, Any)])

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",", "(", ")")
  lexical.reserved ++= List("ObjectId")

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray

  import lexical._

  def makeId(id: String): IdType

  def value: Parser[Any] = id | stringLit | int | double | array | obj

  def anyKeyword: Parser[String] = elem("keyword", _.isInstanceOf[Keyword]) ^^ (_.chars)

  def variable = elem("var", _.isInstanceOf[Variable]) ^^^ Placeholder

  def int: Parser[Int] = numericLit ^^ (_.toInt)

  def double: Parser[Double] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (_.chars.toDouble)

  def objectIdValue = acceptIf(t => t.isInstanceOf[StringLit] && t.chars.length() == 24 && t.chars.forall(hexDigits.contains))(t => "Invalid object id: " + t.chars) ^^ (v => v.chars)

  def id: Parser[IdType] = keyword("ObjectId") ~> "(" ~> objectIdValue <~ ")" ^^ makeId

  def array: Parser[List[Any]] = ("[" ~> repsep(value, ",") <~ "]")

  def member: Parser[(String, Any)] = (ident | anyKeyword) ~ ":" ~ (value | variable) ^^ {
    case i ~ _ ~ v => (i, v)
  }

  def obj: Parser[Object] = "{" ~> repsep(member, ",") <~ "}" ^^ Object

  def parse(expr: String): Object = {
    phrase(obj)(new lexical.Scanner(expr)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }

  def parse(parts: List[String]): Object = {
    val rs = parts.map(p => new CharArrayReader(p.toCharArray))
    phrase(obj)(new lexical.Scanner(rs)) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }
}