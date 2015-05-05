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

package com.github.limansky.mongoquery

import scala.language.experimental.macros
import net.fehmicansaglam.bson.BsonDocument
import tepkin.TepkinMacro

package object tepkin {

  class QueryWrapper {
    def apply[T]: BsonDocument = macro TepkinMacro.t_mqt_impl[T]
  }

  implicit class CasbahQueryHelper(val sc: StringContext) extends AnyVal {
    def mq(args: Any*): BsonDocument = macro TepkinMacro.t_mq_impl

    def mqt(args: Any*) = new QueryWrapper
  }
}
