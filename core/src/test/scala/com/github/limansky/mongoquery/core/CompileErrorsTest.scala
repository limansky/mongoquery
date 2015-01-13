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

  val importStr: String

  def wi(s: String) = importStr + "\n" + s

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

}