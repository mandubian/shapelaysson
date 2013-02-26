
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
import org.specs2.mutable._

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.{Step, Fragments}

import play.api.libs.json._
import play.api.libs.functional._
import play.api.libs.functional.syntax._

import shapeless._
import HList._
import Tuples._
import shapelaysson._

class JsonTestSpec extends Specification {

  "shapelaysson" should {
    
    "validate lots of stuff" in {

      Json.obj().validate[ HNil ].get must beEqualTo( HNil )
      Json.arr().validate[ HNil ].get must beEqualTo( HNil )
      JsString("toto").validate[ HNil ] must beEqualTo( JsError("Not empty JsArray or JsObject") )

      JsString("toto").validate[ String :: HNil ].get must beEqualTo( "toto" :: HNil )
      JsString("toto").validate[ Long :: HNil ] must beEqualTo( JsError("validate.error.expected.jsnumber")  )
      JsNumber(123L).validate[ Long :: HNil ].get must beEqualTo( 123L :: HNil )

      Json.arr("foo", 123L).validate[ String :: Long :: HNil ].get must beEqualTo( "foo" :: 123L :: HNil )
      Json.arr("foo", 123L).validate[ Long :: Long :: HNil ] must beEqualTo( JsError("validate.error.expected.jsnumber") )

      Json.obj("foo" -> "toto", "bar" -> 123L).validate[ String :: Long :: HNil ].get must beEqualTo( "toto" :: 123L :: HNil )
      Json.obj("foo" -> "toto", "bar" -> 123L).validate[ Long :: Long :: HNil ] must beEqualTo( JsError("validate.error.expected.jsnumber") )

      Json.obj(
        "foo" -> "toto", 
        "foofoo" -> Json.obj("barbar1" -> 123.45, "barbar2" -> "tutu"),
        "bar" -> 123L,
        "barbar" -> Json.arr(123, true, "blabla")
      ).validate[ String :: (Float :: String :: HNil) :: Long :: (Int :: Boolean :: String :: HNil) :: HNil ].get must beEqualTo( 
        "toto" :: (123.45F :: "tutu" :: HNil) :: 123L :: (123 :: true :: "blabla" :: HNil) :: HNil
      )

    }

    "transform Reads[Product] to Reads[HList]" in {
      val HListReads: Reads[ String :: Long :: HNil ] = (
        (__ \ "foo").read[String] and
        (__ \ "bar").read[Long]
      ).tupled.hlisted

      Json.obj("foo" -> "toto", "bar" -> 123L).validate(HListReads).get must 
        beEqualTo("toto" :: 123L :: HNil)

      val HListReads2 = (
        (__ \ "foo").read[String] and
        (__ \ "bar").read[Long] and
        (__ \ "toto").read(
          (
            (__ \ "alpha").read[String] and
            (__ \ "beta").read[Boolean]
          ).tupled.hlisted
        )
      ).tupled.hlisted

      Json.obj(
        "foo" -> "toto", 
        "bar" -> 123L,
        "toto" -> Json.obj(
          "alpha" -> "chboing",
          "beta" -> true
        )
      ).validate(HListReads2).get must 
        beEqualTo("toto" :: 123L :: ("chboing" :: true :: HNil) :: HNil)

    }

    "transform Writes[Product] to Writes[HList]" in {
      implicit val HListWrites: Writes[ String :: Long :: HNil ] = (
        (__ \ "foo").write[String] and
        (__ \ "bar").write[Long]
      ).tupled.hlisted

      Json.toJson("toto" :: 123L :: HNil) must 
        beEqualTo(Json.obj("foo" -> "toto", "bar" -> 123L))

      implicit val HListWrites2 = (
        (__ \ "foo").write[String] and
        (__ \ "bar").write[Long] and
        (__ \ "toto").write(
          (
            (__ \ "alpha").write[String] and
            (__ \ "beta").write[Boolean]
          ).tupled.hlisted
        )
      ).tupled.hlisted

      Json.toJson("toto" :: 123L :: ("chboing" :: true :: HNil) :: HNil) must 
        beEqualTo(
          Json.obj(
            "foo" -> "toto", 
            "bar" -> 123L,
            "toto" -> Json.obj(
              "alpha" -> "chboing",
              "beta" -> true
            )
          )
        )
    }

  }

}
