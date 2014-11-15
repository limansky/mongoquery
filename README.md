MongoQuery
----------

MongoDB query builder for Scala.

Currently the MongoDB queries API requires to construct DBObjects explicitly.
This makes even simple queries bulky.  Even though you are using Casbah DSL
it often still requires to create `MongoDBObject`s, and also requires to
study new syntax, instead of using MongoDB queries.  The purpose of this
project is to provide a simple API for creating queries from strings.  The
goal is to make compile time queries syntax checking (as much as possible).

*ATTENTION:* The project is on early stage of development,
so API is not stabilized at all (possible package name will be changed, etc).
Please contact to me if you are going to use it in real project.

In current version it is possible to convert String to `MongoDBObject` using
string interpolator `mq`.

```Scala
import com.github.limansky.mongoquery.casbah._

def findByName(name: String) = {
  myCollection.find(mq"""{ name : $name}""")
}

```

  Current development build status:
  [![Build Status](https://travis-ci.org/limansky/mongoquery.svg?branch=master)](https://travis-ci.org/limansky/mongoquery)
