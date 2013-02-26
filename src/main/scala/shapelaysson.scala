
/*
 * Copyright 2012 Pascal Voitot (@mandubian)
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

package object `shapelaysson` {
  import shapeless._
  import HList._
  import Tuples._

  import play.api.libs.json._
  import play.api.libs.functional._

  private def toHList[H : Reads, T <: HList : Reads](l: List[JsValue])(implicit applicative: Applicative[JsResult]): JsResult[H :: T] = l match {
    case scala.::(head, tail) => 
      applicative.apply( 
        //JsResult[T => H :: T]
        applicative.map(
          implicitly[Reads[H]].reads(head), 
          (h: H) => (t: T) => h :: t
        ),
        //JsResult[T] 
        implicitly[Reads[T]].reads(JsArray(tail)) 
      )  
    case _ => JsError("can't convert empty list using multi-element HList")
  }

  implicit def HNilReads = Reads[HNil]{ js => js match {
    case JsArray(values) if(values.isEmpty)   => JsSuccess(HNil) 
    case JsObject(values) if(values.isEmpty)  => JsSuccess(HNil) 
    case _                                    => JsError("Not empty JsArray or JsObject")
  } }

  implicit def hlistHNilReads[H : Reads](implicit applicative: Applicative[JsResult]) = Reads[H :: HNil]{ js =>       
    js match {
      case arr: JsArray   => toHList[H, HNil](arr.value.toList)
      case obj: JsObject  => toHList[H, HNil](obj.values.toList)
      case js             => implicitly[Reads[H]].reads(js) map { h => h :: HNil }
    }
  }

  implicit def hlistReads[H : Reads, T <: HList : Reads](implicit applicative: Applicative[JsResult]) = Reads[H :: T]{ js =>       
    js match {
      case arr: JsArray   => toHList[H, T](arr.value.toList)
      case obj: JsObject  => toHList[H, T](obj.values.toList)
      case js             => JsError("Single JsValue can't be mapped to multi-element HList")
    }
  }

  implicit def HNilWrites = Writes[HNil]{ hl => JsArray() }

  implicit def hlistHNilWrites[H : Writes] = Writes[H :: HNil]{ hl => 
    val head :: HNil = hl
    JsArray(Seq(implicitly[Writes[H]].writes(head)))
  }

  implicit def hlistWrites[H : Writes, T <: HList : Writes] = Writes[H :: T]{ hl => 
    val head :: tail = hl
    implicitly[Writes[H]].writes(head) +: implicitly[Writes[T]].writes(tail).as[JsArray]
  }

  implicit class TupleReadsOps[ P <: Product ](r: Reads[P]) {
    def hlisted(implicit hlister : HLister[P]) = r map { _.hlisted }
  }

  implicit class TupleWritesOps[ P <: Product ](w: Writes[P]) {    
    def contramap[A, B](wa:Writes[A], f: B => A): Writes[B] = Writes[B]( b => wa.writes(f(b)) )
    def hlisted[T <: HList](implicit hlister : HLister[P], tupler: TuplerAux[T, P]) = contramap(w, (hl: T) => hl.tupled)
  }

}