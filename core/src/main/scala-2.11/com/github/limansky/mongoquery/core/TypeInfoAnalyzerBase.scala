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

abstract class TypeInfoAnalyzerBase[C <: Context](val c: C) {

  def getEffectiveType(tpe: c.Type): c.Type = {
    if (tpe <:< c.typeOf[Option[_]] || tpe <:< c.typeOf[Traversable[_]]) {
      tpe.typeArgs.head
    } else {
      tpe
    }
  }

  def getFields(tpe: c.Type): Map[String, c.Symbol] = {
    import c.universe._

    val ctor = tpe.decl(termNames.CONSTRUCTOR).asMethod
    val params = ctor.paramLists.head

    params.map(s => s.name.toString -> s).toMap
  }
}
