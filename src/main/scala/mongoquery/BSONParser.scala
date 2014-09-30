package mongoquery

import scala.util.parsing.combinator.JavaTokenParsers

trait BSONParser extends JavaTokenParsers {

  def value: Parser[MongoValue[_]] = string | int | double | array | obj

  def string: Parser[MongoString] = stringV ^^ MongoString

  def stringV: Parser[String] = (stringLiteral | singleQuotedString) ^^ { case s => s.substring(1, s.length() -1) }

  def singleQuotedString: Parser[String] = ("'"+"""([^'\p{Cntrl}\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*+"""+"'").r

  def int: Parser[MongoInt] = wholeNumber <~ not('.') ^^ { case w => MongoInt(w.toInt) }

  def double: Parser[MongoDouble] = floatingPointNumber ^^ { case f => MongoDouble(f.toDouble) }

  def array: Parser[MongoArray] = "[" ~> repsep(value, ",") <~ "]" ^^ MongoArray

  def member: Parser[(String, MongoValue[_])] = (ident | stringV) ~ ":" ~ value ^^ { case i~_~v => (i, v) }

  def obj: Parser[MongoObject] = "{" ~> repsep(member, ",") <~ "}" ^^ { case kvs => MongoObject(kvs.toMap)}
}

object BSONParser extends BSONParser {
  def parse(bson: String): MongoObject = {
    parseAll(obj, bson) match {
      case Success(r, _) => r
      case NoSuccess(m, _) => throw new IllegalArgumentException(m)
    }
  }
}
