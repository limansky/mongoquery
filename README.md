MongoQuery
==========

[![Build Status](https://travis-ci.org/limansky/mongoquery.svg?branch=master)](https://travis-ci.org/limansky/mongoquery)
[![codecov](https://codecov.io/gh/limansky/mongoquery/branch/master/graph/badge.svg)](https://codecov.io/gh/limansky/mongoquery)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.limansky/mongoquery-core_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.limansky/mongoquery-core_2.11)

MongoQuery is a macro based MongoDB query builder for Scala.

Currently the [MongoDB][] queries API requires to construct DBObjects explicitly.
This makes even simple queries bulky.  Even though you are using Casbah DSL
it often still requires to create `MongoDBObject`s, and also requires to
study new syntax, instead of using MongoDB queries.  The purpose of this
project is to provide a simple API for creating queries from strings.  The
goal is to make compile time queries syntax checking (as much as possible).

How to use
----------

### Installation ###

Since version 0.7 MongoQuery supports only Scala 2.11 and 2.12.  If you are still using Scala 2.10
you can use MongoQuery 0.6 (or any version before).

MongoQuery is published to Sonatype maven repository.  Add following dependency to
libraryDependencies in your SBT build file:

```
"com.github.limansky" %% "mongoquery-casbah" % "0.7"       // for Casbah users
"com.github.limansky" %% "mongoquery-reactive" % "0.7"     // for ReactiveMongo users
"com.github.limansky" %% "mongoquery-scala-driver" % "0.7" // for Mongo Scala driver
```

If you want use latest development version:

```
"com.github.limansky" %% "mongoquery-casbah" % "0.8-SNAPSHOT"       // Casbah users
"com.github.limansky" %% "mongoquery-reactive" % "0.8-SNAPSHOT"     // ReactiveMongo users
"com.github.limansky" %% "mongoquery-scala-driver" % "0.8-SNAPSHOT" // for Mongo Scala driver
```

### mq interpolator ###

The `mq` string interpolator converts string to the BSON objects. If you use
[Casbah][] it creates `DBObjects`:

```Scala
import com.github.limansky.mongoquery.casbah._

def findByName(name: String) = {
  myCollection.find(mq"{ name : $name}")
}

```

For [ReactiveMongo][] it creates `BSONDocument`s:

```Scala
import com.github.limansky.mongoquery.reactive._

collection.
  find(mq"""{ firstName : "Jack" }""", mq"{ lastName : 1, _id : 1 }").
  cursor[BSONDocument].
  enumerate().apply(Iteratee.foreach { doc =>
  println("found document: " + BSONDocument.pretty(doc))
})
```

For [Mongo Scala Driver][] it creates `BsonDocument`s:

```Scala
import com.github.limansky.mongoquery.scaladriver._

collection.insertOne(mq"""{ name: "John", lastName: "Doe", age : 42 }""")
```

Since the query is defined inside of the string interpolator, the words started
with `$` are handled as variable references.  To type MongoDB operator use `$$`, e.g:

```Scala
def makeOlder(age: Int) = {
  people.update(mq"""{ age : { $$lt : $age } }""",
                mq"""{ $$inc : { age : 1 }}""",
                multi = 1)
}
```

Since the interpolator is implemented using macro it can perform compile time checks
of provided queries. The code will not compile if the query is malformed.  Also
MongoQuery checks if all MongoDB operators are known.

```Scala
[error] Test.scala:44: Unknown operator '$kte'. Possible you mean '$lte'
[error]     val query = mq"{start : {$$kte : $start}}"
[error]                              ^

[error] Test.scala:49: '{' expected, but Variable found
[error]     val q = mq"{ color : {$$in : $colors}"
[error]                                ^
```

Unfortunately, some errors messages does not reflect the error itself.  I'm working
on it, but it seems like the issue in the Scala Parser Combinators library.

### Built-in types ###

MongoQuery supports several MongoDB specific literal types.

 - ObjectIds. `mq"""{ clientId : ObjectId("01234567890abcdef1234") }"""`
 - Booleans. `mq"{ expired : false }"`
 - Regular expressions (since 0.5). `mq"{ name : /^joe/i }"`

### mqt interpolator ###

`mqt` is another one interpolator adding type checking feature.  If you have a model
classes, you can check if the query contains only fields available in the class. E.g.:

```Scala
case class Phone(kind: String, number: String)
case class Person(name: String, age: Int, phones: List[Phone])

// OK
persons.update(mq"{}", mqt"{$$inc : { age : 1 }}"[Person])

// Failed, person doesn't contain field 'nme'
persons.update(mq"{}", mqt"""{$$set : { nme : "Joe" }}"""[Person])

//Failed, name is not indexed field
persons.find(mqt"{ name.1 : 'Joe' }"[Person])

// OK
persons.find(mqt"{ phone.number : '223322' }"[Person])

// Failed, Phone doesn't contain field num
persons.find(mqt"{ phone.num : '223322' }"[Person])
```

### Runtime parsing ###

MongoQuery also provides runtime parsers for both backends. It might be useful for
testing purposes, or if you generate queries on runtime, or for converting JSON to
BSON.  For example:

```Scala
import com.github.limansky.mongoquery.casbah.BSONParser

persons.find(BSONParser.parse("""{ age : { $lt : 42 }}"""))
```

Feedback
--------

Any feedback is very welcome!  You can ask any questions in [MongoQuery mailing list][maillist].

[MongoDB]: http://www.mongodb.org/
[Casbah]: https://github.com/mongodb/casbah
[ReactiveMongo]: http://reactivemongo.org/
[Mongo Scala Driver]: http://mongodb.github.io/mongo-scala-driver/
[maillist]: https://groups.google.com/forum/#!forum/mongoquery-users
