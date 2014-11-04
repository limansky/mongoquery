package mongoquery.bsonparser

import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.input.Reader
import scala.util.parsing.input.CharArrayReader

class Lexical extends StdLexical with BSONTokens {

  override def whitespace = rep(whitespaceChar)

  override def token = (
    ident ^^ { case x ~ xs => Identifier(x :: xs mkString "") }
    | rep1(digit) ~ '.' ~ rep1(digit) ^^ { case xs ~ '.' ~ ys => DoubleLit((xs ::: '.' :: ys).mkString) }
    | ('$' ~> ident) ^^ { case x ~ xs => Keyword('$' :: x :: xs mkString "") }
    | super.token
  )

  def ident = identChar ~ rep(identChar | digit)

  def wrapQuotes[T](p: Parser[T]): Parser[T] = ('"' ~> p <~ '"') | ('\'' ~> p <~ '\'')

  class Scanner(s: super.Scanner, readers: List[Reader[Char]], args: Seq[Any]) extends Reader[Token] { // super.Scanner(in) {

    def this(readers: List[Reader[Char]], args: Seq[Any]) = this(new super.Scanner(readers.head), readers.tail, args)

    def this(in: String) = this(new super.Scanner(in), Nil, Nil)

    override def first = {
      if (s.atEnd && readers.nonEmpty) {
        Variable(args.head)
      } else {
        s.first
      }
    }

    override def rest = {
      if (s.atEnd && readers.nonEmpty) {
        new Scanner(new Lexical.super.Scanner(readers.head), readers.tail, args.tail)
      } else {
        new Scanner(s.rest, readers, args)
      }
    }

    override def atEnd = s.atEnd && readers.isEmpty

    override def pos = s.pos
  }
}
