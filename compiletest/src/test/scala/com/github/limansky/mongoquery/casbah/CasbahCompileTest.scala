package com.github.limansky.mongoquery.casbah

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import tools.reflect.ToolBox
import reflect.runtime.{ universe => ru }

class CasbahCompileTest extends FlatSpec with Matchers {

  def wci(code: String) = "import com.github.limansky.mongoquery.casbah._\n" + code

  "Casbah mq" should "fail on malformed BSON" in {
    val tb = ru.runtimeMirror(getClass.getClassLoader).mkToolBox()
    tb.eval(tb.parse(wci("""
    object TestMe {
      val q = mq"{ a 5 }"
    }""")))
  }
}
