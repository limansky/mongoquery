MongoQuery
----------

[![Build Status](https://travis-ci.org/limansky/mongoquery.svg?branch=master)](https://travis-ci.org/limansky/mongoquery)

MongoDB is a macro based query builder for Scala.

Currently the [MongoDB][] queries API requires to construct DBObjects explicitly.
This makes even simple queries bulky.  Even though you are using Casbah DSL
it often still requires to create `MongoDBObject`s, and also requires to
study new syntax, instead of using MongoDB queries.  The purpose of this
project is to provide a simple API for creating queries from strings.  The
goal is to make compile time queries syntax checking (as much as possible).

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
collection.
  find(mq"""{ firstName : "Jack" }""", mq"{ lastName : 1, _id : 1 }").
  cursor[BSONDocument].
  enumerate().apply(Iteratee.foreach { doc =>
  println("found document: " + BSONDocument.pretty(doc))
})
```

Current development build status:

[MongoDB]: http://www.mongodb.org/
[Casbah]: https://github.com/mongodb/casbah
[ReactiveMongo]: http://reactivemongo.org/
