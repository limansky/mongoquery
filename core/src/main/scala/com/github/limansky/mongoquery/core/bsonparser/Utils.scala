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

object Utils {

  def levenshtein(f: String, s: String): Int = {
    import math.min
    s.foldLeft((0 to f.length).toList)((l, a) =>
      (l, l.tail, f).zipped.toList.scanLeft(l.head + 1) {
        case (del, (rep, ins, b)) =>
          val c = if (a == b) 0 else 1
          min(min(del + 1, ins + 1), rep + c)
      }).last
  }
}