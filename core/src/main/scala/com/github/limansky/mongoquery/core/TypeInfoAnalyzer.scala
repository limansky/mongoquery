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

import com.github.limansky.mongoquery.core.BSON.{ IdentPart, IndexedField, Member }
import com.github.limansky.mongoquery.core.TypeInfoAnalyzer.ValidationResult

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object TypeInfoAnalyzer {
  type ValidationResult = Either[String, (Member, Any)]
}

class TypeInfoAnalyzer[C <: blackbox.Context](val c: C) {

  import c.universe._

  @tailrec
  private def doCheck(pair: (Member, Any), tpe: c.Type, parts: List[IdentPart]): ValidationResult = {

    val t = getEffectiveType(tpe)
    val f :: fs = parts

    getFields(t).get(f.name) match {
      case Some(s) =>
        f match {
          case IndexedField(_, _) if !(s.typeSignature <:< typeOf[Traversable[_]]) =>
            Left(s"Field ${f.name} of type ${s.typeSignature} cannot be indexed")

          case _ if fs.nonEmpty =>
            val nested = s.typeSignature
            doCheck(pair, nested, fs)

          case _ => Right(pair)
        }

      case None =>
        Left(s"Class ${t.typeSymbol.name.toString} doesn't contain field '${f.name}'")
    }
  }

  def check(tpe: c.Type)(pair: (Member, Any)): ValidationResult = {
    doCheck(pair, tpe, pair._1.fields)
  }

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
