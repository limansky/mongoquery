MongoQuery
==========

[![Build Status](https://travis-ci.org/limansky/mongoquery.svg?branch=master)](https://travis-ci.org/limansky/mongoquery)
[![Coverage Status](https://coveralls.io/repos/limansky/mongoquery/badge.png)](https://coveralls.io/r/limansky/mongoquery)

MongoQuery is a macro based MongoDB query builder for Scala.

Currently the [MongoDB][] queries API requires to construct DBObjects explicitly.
This makes even simple queries bulky.  Even though you are using Casbah DSL
it often still requires to create `MongoDBObject`s, and also requires to
study new syntax, instead of using MongoDB queries.  The purpose of this
project is to provide a simple API for creating queries from strings.  The
goal is to make compile time queries syntax checking (as much as possible).

How to use
----------

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

Since the query is defined inside of the string interpolator, the words started
with `$` are handled as variable references.  To type MongoDB operator use `$$`, e.g:

```Scala
def makeOlder(age: Int) = {
  people.update(mq"""{ age : { $$lt : $age } }""",
                mq"""{ $$inc : { age : 1 }}""",
                multi = 1)
}
```

Installation
------------

MongoQuery is published to Sonatype maven repository.  Add following dependency to
libraryDependencies in your SBT build file:

```
"com.github.limansky" %% "mongoquery-casbah" % "0.3"    // for Casbah users
"com.github.limansky" %% "mongoquery-reactive" % "0.3"  // for ReactiveMongo users
```

If you want use latest development version:

```
"com.github.limansky" %% "mongoquery-casbah" % "0.4-SNAPSHOT"    // Casbah users
"com.github.limansky" %% "mongoquery-reactive" % "0.4-SNAPSHOT"  // ReactiveMongo users
```

Feedback
--------

Any feedback is very welcome!  You can ask any questions in [MongoQuery mailing list][maillist].

[MongoDB]: http://www.mongodb.org/
[Casbah]: https://github.com/mongodb/casbah
[ReactiveMongo]: http://reactivemongo.org/
[maillist]: https://groups.google.com/forum/#!forum/mongoquery-users
