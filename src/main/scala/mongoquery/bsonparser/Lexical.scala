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

  class Scanner(in: Reader[Char], readers: List[Reader[Char]], args: Seq[Any]) extends super.Scanner(in) {

    def this(readers: List[Reader[Char]], args: Seq[Any]) = this(readers.head, readers.tail, args)

    def this(in: String) = this(new CharArrayReader(in.toCharArray()), Nil, Nil)

    override def first = {
      if (super.atEnd && readers.nonEmpty) {
        Variable(args.head)
      } else {
        super.first
      }
    }

    override def rest = {
      if (super.atEnd && readers.nonEmpty) {
        new Scanner(readers.head, readers.tail, args.tail)
      } else {
        super.rest
      }
    }

    override def atEnd = super.atEnd && readers.isEmpty
  }
}
