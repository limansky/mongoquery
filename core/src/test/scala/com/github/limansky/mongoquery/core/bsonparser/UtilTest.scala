/*
 * Copyright 2014 Mike Limansky
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
package com.github.limansky.mongoquery.core.bsonparser

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.{GeneratorDrivenPropertyChecks, TableDrivenPropertyChecks}

class UtilTest extends FlatSpec with Matchers
    with GeneratorDrivenPropertyChecks
    with TableDrivenPropertyChecks {

  "Levenshtein" should "be zero for matching strings" in {
    forAll { s: String =>
      Utils.levenshtein(s, s) should be(0)
    }
  }

  it should "be equals string length if 1st empty" in {
    forAll { s: String =>
      Utils.levenshtein(s, "") should be(s.length())
    }
  }

  it should "be equals string length if 2nd empty" in {
    forAll { s: String =>
      Utils.levenshtein("", s) should be(s.length())
    }
  }

  it should "be commutative" in {
    forAll { (a: String, b: String) =>
      Utils.levenshtein(a, b) should equal(Utils.levenshtein(b, a))
    }
  }

  val strings = Table(
    ("first", "second", "expected"),
    ("match", "mathc", 2),
    ("foo", "bar", 3),
    ("first", "fist", 1),
    ("zo", "zoo", 1)
  )

  it should "show difference for strings" in {
    forAll(strings) { (f, s, e) =>
      Utils.levenshtein(f, s) should be(e)
    }
  }

}
