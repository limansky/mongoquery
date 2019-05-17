package play

import com.github.limansky.mongoquery.scala_driver._
import org.mongodb.scala.MongoCollection

object Playground extends App {

  case class Phone(kind: String, number: String)
  case class Person(name: String, age: Int, phones: List[Phone])

  val collection: MongoCollection[Person] = ???

  def findByName(name: String) = {
    collection.find(mqt"{ phones.1.number : $name }"[Person])
  }

}
