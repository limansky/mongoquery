package mongoquery.bsonparser

import scala.util.parsing.combinator.token.StdTokens

trait BSONTokens extends StdTokens {

  case class DoubleLit(chars: String) extends Token

  case class Variable(v: Any) extends Token {
    def chars = "variable"
  }

}
