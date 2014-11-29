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

import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.input.Reader
import scala.util.parsing.input.CharArrayReader
import scala.collection.mutable

class Lexical extends StdLexical with BSONTokens {

  val operators = new mutable.HashSet[String]

  override def whitespace = rep(whitespaceChar)

  override def token = (
    float ^^ DoubleLit
    | '-' ~> number ^^ { case n => NumericLit('-' + n) }
    | knownOperator
    | ident ^^ processIdent
    | super.token
  )

  def number = rep1(digit) ^^ (_.mkString)

  def sign = opt(elem('+') | '-') ^^ (_.map(_.toString) getOrElse "")

  def simpleFloat = sign ~ number ~ '.' ~ rep(digit) ^^ {
    case s ~ xs ~ '.' ~ ys =>
      s + xs + ('.' :: ys).mkString
  }

  def expFloat = sign ~ number ~ opt('.' ~> number) ~ (elem('e') | 'E') ~ sign ~ number ^^ {
    case s ~ n ~ f ~ e ~ es ~ en =>
      val fl = f map { case n => '.' + n } getOrElse ""
      s + n + fl + e + es + en
  }

  def float = expFloat | simpleFloat

  def operator = '$' ~ rep(letter) ^^ {
    case x ~ xs =>
      x :: xs mkString ""
  }

  def knownOperator = operator ^? ({
    case o if operators.contains(o) => Operator(o)
  }, u => {
    val p = operators.map(o => (o, Utils.levenshtein(u, o))).minBy(_._2)._1
    s"Unknown operator '$u'. Possible you mean '$p'"
  }
  )

  def ident = field ~ rep('.' ~> (field | index)) ^^ { case p ~ c => p :: c mkString "." }

  def index = ('$' ^^^ "$") | number

  def field = identChar ~ rep(identChar | digit) ^^ { case x ~ xs => (x :: xs).mkString }

  class Scanner(s: super.Scanner, readers: List[Reader[Char]], val part: Int) extends Reader[Token] {

    def this(readers: List[Reader[Char]]) = this(new super.Scanner(readers.head), readers.tail, 0)

    def this(in: String) = this(new super.Scanner(in), Nil, 0)

    override def first = {
      if (s.atEnd && readers.nonEmpty) {
        Variable
      } else {
        s.first
      }
    }

    override def rest = {
      if (s.atEnd && readers.nonEmpty) {
        new Scanner(new Lexical.super.Scanner(readers.head), readers.tail, part + 1)
      } else {
        new Scanner(s.rest, readers, part)
      }
    }

    override def atEnd = s.atEnd && readers.isEmpty

    override def pos = s.pos

    override def offset = s.offset
  }
}
