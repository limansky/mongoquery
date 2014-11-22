/*
 * Copyright 2014 Mike Limansky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.limansky.mongoquery.core.bsonparser

import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.input.CharArrayReader

trait Parser extends StdTokenParsers {

  override type Tokens = BSONTokens

  case object Placeholder
  case class Object(members: List[(String, Any)])
  case class Id(id: String)
  case class DateTime(l: Long)

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",", "(", ")")
  lexical.reserved ++= List("ObjectId", "true", "false")

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray

  import lexical._

  def value: Parser[Any] = id | stringLit | int | double | boolean | array | obj

  def anyKeyword: Parser[String] = elem("keyword", _.isInstanceOf[Keyword]) ^^ (_.chars)

  def variable = elem("var", _.isInstanceOf[Variable]) ^^^ Placeholder

  def int: Parser[Int] = numericLit ^^ (_.toInt)

  def boolean: Parser[Boolean] = trueLit | falseLit

  def trueLit: Parser[Boolean] = keyword("true") ^^^ true

  def falseLit: Parser[Boolean] = keyword("false") ^^^ false

  def double: Parser[Double] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (_.chars.toDouble)

  def objectIdValue = acceptIf(t => t.isInstanceOf[StringLit] && t.chars.length() == 24 && t.chars.forall(hexDigits.contains))(t => "Invalid object id: " + t.chars) ^^ (v => v.chars)

  def id: Parser[Id] = keyword("ObjectId") ~> "(" ~> objectIdValue <~ ")" ^^ Id

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
