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

import com.github.limansky.mongoquery.core.BSON.Member
import scala.util.parsing.combinator.syntactical.StdTokenParsers
import scala.util.parsing.input.CharArrayReader

class Parser extends StdTokenParsers {

  import com.github.limansky.mongoquery.core.BSON._

  override type Tokens = BSONTokens

  val queryOperators = Set(
    "$gt", "$gte", "$lt", "$lte", "$ne", "$in", "$nin", // Compare
    "$and", "$or", "$nor", "$not", // Logical
    "$exists", "$type", // Element
    "$mod", "$regex", "$text", "$where", // Evaluation
    "$geoIntersects", "$geoWithin", "$nearSphere", "$near", // Geospatial
    "$all", "$elemMatch", "$size", // Array
    "$comment", "$meta", "$slice" // Other
  )

  val updateOperators = Set(
    "$currentDate", "$inc", "$max", "$min", "$mul", "$rename", "$setOnInsert", "$set", "$unset", //Fields
    "$addToSet", "$pop", "$push", "$pushAll", "$pull", "$pullAll", // Array
    "$each", "$position", "$sort", // Modifiers
    "$bit", "$isolated" // Other
  )

  val aggregationOperators = Set(
    "$geoNear", "$group", "$limit", "$match", "$out", "$project", "$redact", "$slip", "$sort", "$unwind", // Stage
    "$and", "$not", "$or", // Boolean
    "$allElementsTrue", "$anyElementTrue", "$setDifference", "$setIntersection", "$setIsSubset", "$setUnion", // Sets
    "$cmp", "$eq", "$gt", "$gte", "$lt", "$lte", "$ne", // Comparation
    "$add", "$divide", "$mod", "$multiply", "$subtract", // Arithmetic
    "$concat", "$strcasecmp", "$substr", "$toLower", "$toUpper", // String
    "$meta", // Text search
    "$size", // Array
    "$let", "$map", // Variable
    "$literal", // Literal
    "$dayOfMonth", "$dayOfWeek", "$dayOfYear", "$hour", "$millisecond",
    "$minute", "$month", "$second", "$week", "$year", // Date
    "$cond", "$ifNull", // Conditional
    "$addToSet", "$avg", "$first", "$last", "$max", "$min", "$push", "$sum" // Accumulator
  )

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",", "(", ")")
  lexical.reserved ++= List("ObjectId", "true", "false", "null")
  lexical.operators ++= queryOperators ++ updateOperators ++ aggregationOperators

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray

  import lexical._

  def value: Parser[Any] = id | regexLit | stringLit | int | double | boolean | nullLit | array | obj | variable

  def operator: Parser[Operator] = elem("operator", _.isInstanceOf[OperatorLit]) ^^ (o => Operator(o.chars))

  def variable = elem("variable", _ == Variable) ^^^ Placeholder

  def int: Parser[Int] = numericLit ^^ (_.toInt)

  def boolean: Parser[Boolean] = trueLit | falseLit

  def trueLit: Parser[Boolean] = keyword("true") ^^^ true

  def falseLit: Parser[Boolean] = keyword("false") ^^^ false

  def nullLit: Parser[Null] = keyword("null") ^^^ null

  def double: Parser[Double] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (_.chars.toDouble)

  def objectIdValue = acceptIf(t => t.isInstanceOf[StringLit] && t.chars.length() == 24 && t.chars.forall(hexDigits.contains))(t => "Invalid object id: " + t.chars) ^^ (v => v.chars)

  def id: Parser[Id] = keyword("ObjectId") ~> "(" ~> objectIdValue <~ ")" ^^ Id

  def regexLit: Parser[Regex] = elem("regex", _.isInstanceOf[RegexLit]) ^^ { case RegexLit(e, o) => Regex(e, o) }

  def fields: Parser[Member] = elem("fields", _.isInstanceOf[FieldLit]) ^^ { case FieldLit(p) => Member(p) }

  def array: Parser[List[Any]] = "[" ~> repsep(value, ",") <~ "]"

  def member: Parser[(LValue, Any)] = (fields | operator) ~ ":" ~ value ^^ {
    case i ~ _ ~ v => (i, v)
  }

  def obj: Parser[Object] = "{" ~> repsep(member, ",") <~ "}" ^^ Object

  override def accept(e: Elem): Parser[Elem] = acceptIf(_ == e) {
    case lexical.ErrorToken(c) => c.stripPrefix("*** error: ")
    case in => "" + e + " expected, but " + in + " found"
  }

  def parse(expr: String): ParseResult[Object] = {
    phrase(obj)(new lexical.Scanner(expr))
  }

  def parse(parts: List[String]): ParseResult[Object] = {
    val rs = parts.map(p => new CharArrayReader(p.toCharArray))
    phrase(obj)(new lexical.Scanner(rs))
  }
}
