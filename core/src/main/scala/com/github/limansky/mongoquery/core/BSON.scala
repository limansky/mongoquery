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

package com.github.limansky.mongoquery.core

object BSON {
  case object Placeholder
  case class Object(members: List[(LValue, Any)])
  case class Id(id: String)
  case class DateTime(l: Long)

  sealed abstract class IdentPart(val name: String)

  case class Field(override val name: String) extends IdentPart(name)
  case class IndexedField(override val name: String, index: String) extends IdentPart(name)

  sealed abstract class LValue {
    def asString: String
  }

  case class Member(fields: List[IdentPart]) extends LValue {
    override val asString = fields.map {
      case Field(n) => n
      case IndexedField(n, i) => n + "." + i
    } mkString "."
  }

  case class Operator(name: String) extends LValue {
    override val asString = name
  }
}
