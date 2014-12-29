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

import MacroContext.Context
import BSON.Member

abstract class TypeInfoAnalyzer[T](val c: Context) {

  protected def tpe: c.Type

  import c.universe._

  val idents = getFields()

  def getFields(): List[String] = {

    val ctor = tpe.decl(termNames.CONSTRUCTOR).asMethod
    val params = ctor.paramLists.head

    params.map(_.name.toString)
  }

  def check(pair: (Member, Any)) = {
    val (field, value) = pair

    if (idents.contains(field.fields.head.name)) {
      Right(pair)
    } else {
      Left(s"Class ${tpe.toString()} doesn't contain field '$field'")
    }
  }
}
