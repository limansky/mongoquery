/*
 * Copyright 2015 Mike Limansky
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

package com.github.limansky.mongoquery.core

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import scala.reflect.runtime.{ universe => ru }
import scala.tools.reflect.ToolBox
import java.net.URLClassLoader
import java.io.File
import org.scalatest.prop.TableDrivenPropertyChecks
import scala.tools.reflect.ToolBoxError
import scala.reflect.runtime.{ universe => ru }

abstract class CompileErrorsTest extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  val cl = getClass.getClassLoader.asInstanceOf[URLClassLoader]
  val cp = cl.getURLs.map(_.getFile).mkString(File.pathSeparator)

  val mirror = ru.runtimeMirror(cl)
  val tb = mirror.mkToolBox(options = s"-cp $cp")

  def imports = "com.github.limansky.mongoquery.core.TestObjects._" :: Nil

  def wi(s: String) = imports.map("import " + _).reduceLeft(_ + "\n" + _) + "\n" + s

  val malformed = Table(
    ("query", "message"),
    ("""mq"{ a 1 }"""", "`:' expected, but 1 found"),
    ("""mq"{ a : 1"""", "end of input"),
    ("""mq"a : 1}"""", "`{' expected, but a found") //,
  //    ("""mq{a : 1 b : "foo"}""", "`,' expected, but b found")  // weird error
  )

  def checkError(q: String, m: String) = {
    try {
      tb.eval(tb.parse(wi(q)))
    } catch {
      case e: ToolBoxError =>
        e.message should include(m)
    }
  }

  "mq" should "fail on malformed BSON" in {
    forAll(malformed)(checkError)
  }

  it should "fail on unknown operators" in {
    checkError("""mq"{a : { $$exits : true}}"""", "Unknown operator '$exits'. Possible you mean '$exists'")
  }

  val unknownField = Table(
    ("query", "message"),
    ("""mqt"{a : 'bar'}"[Foo]""", "Class Foo doesn't contain field 'a'"),
    ("""mqt"{f.a : 'bar'}"[Bar]""", "Class Foo doesn't contain field 'a'"),
    ("""mqt"{f.a : 'bar'}"[Baz]""", "Class Foo doesn't contain field 'a'"),
    ("""mqt"{lf.1.a : 'bar'}"[Quux]""", "Class Foo doesn't contain field 'a'"),
    ("""mqt"{lf.$$.a : 'bar'}"[Quux]""", "Class Foo doesn't contain field 'a'"),
    ("""mqt"{f.1.s : 'bar'}"[Quux]""", "Class Quux doesn't contain field 'f'")
  )

  "mqt" should "fail if field does not exists" in {
    forAll(unknownField)(checkError)
  }

  val wrongIndexing = Table(
    ("query", "message"),
    ("""mqt"{ s.1 : 'test'}"[Foo]""", "Field s of type String cannot be indexed"),
    ("""mqt"{ i.1 : 42}"[Bar]""", "Field i of type Int cannot be indexed"),
    ("""mqt"{ f.1 : 42}"[Baz]""", "Field f of type Option[com.github.limansky.mongoquery.core.TestObjects.Foo] cannot be indexed")
  )

  it should "fail on indexing of not Traversable" in {
    forAll(wrongIndexing)(checkError)
  }

}
