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
import BSON.{ Member, IndexedField }

abstract class TypeInfoAnalyzer(override val c: Context) extends TypeInfoAnalyzerBase(c) {

  def check(pair: (Member, Any)) = {
    import c.universe._

    val (field, value) = pair
    val idents = getFields()
    val f = field.fields.head

    idents.get(f.name) match {
      case Some(s) =>
        f match {
          case IndexedField(n, i) if !(s.typeSignature <:< typeOf[Traversable[_]]) =>
            Left(s"Field ${f.name} of type ${s.typeSignature} cannot be indexed")
          case _ => Right(pair)
        }
      case None =>
        Left(s"Class ${tpe.toString()} doesn't contain field '${field.asString}'")
    }
  }
}