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

class Lexical extends StdLexical with BSONTokens {

  override def whitespace = rep(whitespaceChar)

  override def token = (
    opt('-') ~ rep1(digit) ~ '.' ~ rep1(digit) ^^ { case sign ~ xs ~ '.' ~ ys => DoubleLit((sign.toList ::: xs ::: '.' :: ys).mkString) }
    | '-' ~> rep1(digit) ^^ { case ds => NumericLit('-' :: ds mkString "") }
    | keyword ^^ { case x ~ xs => Keyword(x :: xs mkString "") }
    | ident ^^ processIdent
    | super.token
  )

  def keyword = '$' ~ rep(letter)

  def ident = field ~ rep('.' ~> (field | index)) ^^ { case p ~ c => p :: c mkString "." }

  def index = ('$' ^^^ "$") | (rep(digit) ^^ (_.mkString))

  def field = identChar ~ rep(identChar | digit) ^^ { case x ~ xs => (x :: xs).mkString }

  def wrapQuotes[T](p: Parser[T]): Parser[T] = ('"' ~> p <~ '"') | ('\'' ~> p <~ '\'')

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
