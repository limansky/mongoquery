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

import com.github.limansky.mongoquery.core.BSON.{ Field, IdentPart, IndexedField }

import scala.util.parsing.input.CharArrayReader.EofCh

import scala.collection.mutable
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.input.Reader

class Lexical extends StdLexical with BSONTokens {

  val operators = new mutable.HashSet[String]

  override def whitespace = rep(whitespaceChar)

  override def token = (
    float ^^ DoubleLit
    | '-' ~> number ^^ { n => NumericLit('-' + n) }
    | knownOperator
    | regex
    | ident ^^ wrapIdent
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
      val fl = f map { n => '.' + n } getOrElse ""
      s + n + fl + e + es + en
  }

  def float = expFloat | simpleFloat

  def operator = '$' ~ rep(letter) ^^ {
    case x ~ xs =>
      x :: xs mkString ""
  }

  def knownOperator = operator ^? (
    {
      case o if operators.contains(o) => OperatorLit(o)
    },
    u => {
      val p = operators.minBy(o => Utils.levenshtein(u, o))
      s"Unknown operator '$u'. Possible you mean '$p'"
    }
  )

  def ident = (fieldOrIndexed | '"' ~> fieldOrIndexed <~ '"') <~ whitespace <~ ':'

  def fieldOrIndexed = rep1sep(indexedField | field, '.')

  def index = ('$' ^^^ "$") | number

  def indexedField = fieldName ~ '.' ~ index ^^ { case f ~ _ ~ i => IndexedField(f, i) }

  def field = fieldName ^^ Field

  def fieldName = identChar ~ rep(identChar | digit) ^^ { case x ~ xs => (x :: xs).mkString }

  def regexChars = chrExcept(EofCh, '\n', '/', '\\')

  def regexOptChars = elem("options", "imxs".contains(_))

  def regexPart = rep1(regexChars) ^^ (_.mkString)

  def escapedChar = '\\' ~> (accept('/') | 'n' | 'r' | 't' | '\\') ^^ ("\\" + _)

  def regex = ('/' ~> rep(regexPart | escapedChar) <~ '/') ~ rep(regexOptChars) ^^ {
    case rc ~ opt => RegexLit(rc.mkString, opt.mkString)
  }

  def wrapIdent(parts: List[IdentPart]): Token = {
    parts match {
      case Field(f) :: Nil if reserved contains f => Keyword(f)
      case _ => FieldLit(parts)
    }
  }

  class Scanner(s: super.Scanner, readers: Seq[Reader[Char]], val part: Int) extends Reader[Token] {

    def this(readers: Seq[Reader[Char]]) = this(new super.Scanner(readers.head), readers.tail, 0)

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
