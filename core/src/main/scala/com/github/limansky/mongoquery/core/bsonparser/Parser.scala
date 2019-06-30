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

import com.github.limansky.mongoquery.core.BSON

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
    // Stages
    "$geoNear", "$group", "$limit", "$match", "$out", "$project", "$redact", "$slip", "$sort", "$unwind",
    "$sample", "$indexStats", "$lookup", "$graphLookup", "$bucket", "$bucketAuto", "$facet", "$sortByCount",
    "$addFields", "$replaceRoot", "$count", "$currentOp", "$listSessions", "$listLocalSessions",
    // Boolean
    "$and", "$not", "$or",
    // Sets
    "$allElementsTrue", "$anyElementTrue", "$setDifference", "$setIntersection", "$setIsSubset", "$setUnion",
    // Comparation
    "$cmp", "$eq", "$gt", "$gte", "$lt", "$lte", "$ne",
    // Arithmetic
    "$add", "$divide", "$mod", "$multiply", "$subtract", "$sqrt", "$abs", "$log", "$log10", "$ln", "$pow",
    "$exp", "$trunc", "$ceil", "$floor",
    // String
    "$concat", "$strcasecmp", "$substr", "$toLower", "$toUpper", "$indexOfBytes", "$indexOfCP", "$split",
    "$strLenBytes", "$strLenCP", "$substrBytes", "$substrCP",
    "$ltrim", "$rtrim", "$trim", // from version 4.x
    // Text search
    "$meta",
    // Array
    "$size", "$slice", "$arrayElemAt", "$concatArrays", "$isArray", "$filter",
    "$indexOfArray", "$range", "$reverseArray", "$reduce", "$zip", "$arrayToObject",
    // Ojbect
    "$objectToArray", "$mergeObjects",
    // Variable
    "$let", "$map",
    // Literal
    "$literal",
    // Date
    "$dayOfMonth", "$dayOfWeek", "$dayOfYear", "$hour", "$millisecond", "$isoDayOfWeek", "$isoWeek", "$isoWeekYear",
    "$minute", "$month", "$second", "$week", "$year", "$dateToString", "$dateFromString", "$dateFromParts", "$dateToParts",
    // Conditional
    "$cond", "$ifNull", "$switch",
    // Accumulator
    "$addToSet", "$avg", "$first", "$last", "$max", "$min", "$push", "$sum", "$stdDevSamp", "$stdDevPop",
    // Other
    "$collStats"
  )

  val bitsOperators = List(
    "$bitsAllSet", "$bitsAllClear", "$bitsAnySet", "$bitsAnyClear"
  )

  // from version 4.x
  val typeConversionOperators = List(
    "$convert", "$toBool", "$toDate", "$toDecimal", "$toDouble", "$toInt", "$toLong", "$toObjectId", "$toString"
  )

  override val lexical = new Lexical
  lexical.delimiters ++= List("[", "]", "{", "}", ":", ",", "(", ")")
  lexical.reserved ++= List("ObjectId", "true", "false", "null")
  lexical.operators ++= queryOperators ++ updateOperators ++ aggregationOperators ++ bitsOperators ++ typeConversionOperators

  val hexDigits = Set[Char]() ++ "0123456789abcdefABCDEF".toArray

  import lexical._

  def value: Parser[Any] = id | regexLit | int | double | boolean | nullLit | array | variable | obj | stringLit

  def operator: Parser[Operator] = elem("operator", _.isInstanceOf[OperatorLit]) ^^ (o => Operator(o.chars))

  def variable: Parser[BSON.Placeholder.type] = elem("variable", _ == Variable) ^^^ Placeholder

  def int: Parser[Int] = numericLit ^^ (_.toInt)

  def boolean: Parser[Boolean] = trueLit | falseLit

  def trueLit: Parser[Boolean] = keyword("true") ^^^ true

  def falseLit: Parser[Boolean] = keyword("false") ^^^ false

  def nullLit: Parser[NullObj.type] = keyword("null") ^^^ NullObj

  def double: Parser[Double] = elem("double", _.isInstanceOf[DoubleLit]) ^^ (_.chars.toDouble)

  def objectIdValue: Parser[String] = acceptIf(t => t.isInstanceOf[StringLit] && t.chars.length() == 24 && t.chars.forall(hexDigits.contains))(t => "Invalid object id: " + t.chars) ^^ (v => v.chars)

  def id: Parser[Id] = keyword("ObjectId") ~> "(" ~> objectIdValue <~ ")" ^^ Id

  def regexLit: Parser[Regex] = elem("regex", _.isInstanceOf[RegexLit]) ^^ { case RegexLit(e, o) => Regex(e, o) }

  def fields: Parser[Member] = elem("fields", _.isInstanceOf[FieldLit]) ^^ { case FieldLit(p) => Member(p) }

  def array: Parser[List[Any]] = "[" ~> repsep(value, ",") <~ "]"

  def member: Parser[(LValue, Any)] = (fields | operator <~ ":") ~ value ^^ {
    case i ~ v => (i, v)
  }

  def obj: Parser[Object] = "{" ~> repsep(member, ",") <~ "}" ^^ Object

  override def accept(e: Elem): Parser[Elem] = acceptIf(_ == e) {
    case lexical.ErrorToken(c) => c.stripPrefix("*** error: ")
    case in => "" + e + " expected, but " + in + " found"
  }

  def parse(expr: String): ParseResult[Object] = {
    phrase(obj)(new lexical.Scanner(expr))
  }

  def parse(parts: Seq[String]): ParseResult[Object] = {
    val rs = parts.map(p => new CharArrayReader(p.toCharArray))
    phrase(obj)(new lexical.Scanner(rs))
  }
}
