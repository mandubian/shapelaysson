# Shapelaysson = Shapeless + Play-Json 

Do you like [Shapeless](https://github.com/milessabin/shapeless), this great API developed by Miles Sabin studying generic/polytypic programming in Scala?

Do you like [Play-json](https://github.com/mandubian/play-json-alone), the Play Json 2.1 Json API developed for Play 2.1 framework providing functional & typesafe Json validation and Scala conversion?

Here is **Shapelaysson** an API interleaving Play-Json with Shapeless to be able to **manipulate Json from/to Shapeless HList heterogenous lists** (_HList are polymorphic lists able to contain different types of data and able to keep tracks of these types_)


> `Shapelaysson` takes part in my reflexions around manipulating pure data structures from/to JSON.

## A few pure Json from/to HList samples

```scala
import play.api.libs.json._
import shapeless._
import HList._
import Tuples._
import shapelaysson._

// validates + converts a JsArray into HList
scala> Json.arr("foo", 123L).validate[ String :: Long :: HNil ]
res1: play.api.libs.json.JsResult[shapeless.::[String,shapeless.::[Long,shapeless.HNil]]] = 
JsSuccess(foo :: 123 :: HNil,)

// validates + converts a JsObject into HList
scala> Json.obj("foo" -> "toto", "bar" -> 123L).validate[ String :: Long :: HNil ]
res3: play.api.libs.json.JsResult[shapeless.::[String,shapeless.::[Long,shapeless.HNil]]] = 
JsSuccess(toto :: 123 :: HNil,)

// validates + converts imbricated JsObject into HList
scala> Json.obj(
     |   "foo" -> "toto", 
     |   "foofoo" -> Json.obj("barbar1" -> 123.45, "barbar2" -> "tutu"),
     |      "bar" -> 123L,
     |      "barbar" -> Json.arr(123, true, "blabla")
     |   ).validate[ String :: (Float :: String :: HNil) :: Long :: (Int :: Boolean :: String :: HNil) :: HNil ]
res4: play.api.libs.json.JsResult[shapeless.::[String,shapeless.::[shapeless.::[Float,shapeless.::[String,shapeless.HNil]],shapeless.::[Long,shapeless.::[shapeless.::[Int,shapeless.::[Boolean,shapeless.::[String,shapeless.HNil]]],shapeless.HNil]]]]] = 
JsSuccess(toto :: 123.45 :: tutu :: HNil :: 123 :: 123 :: true :: blabla :: HNil :: HNil,)

// validates with ERROR JsArray into HList
scala> Json.arr("foo", 123L).validate[ Long :: Long :: HNil ] must beEqualTo( JsError("validate.error.expected.jsnumber") )
<console>:23: error: value must is not a member of play.api.libs.json.JsResult[shapeless.::[Long,shapeless.::[Long,shapeless.HNil]]]
                    Json.arr("foo", 123L).validate[ Long :: Long :: HNil ] must beEqualTo( JsError("validate.error.expected.jsnumber") )

// converts HList to JsValue
scala> Json.toJson(123.45F :: "tutu" :: HNil)
res6: play.api.libs.json.JsValue = [123.44999694824219,"tutu"]

```

## A few Json Reads/Writes[HList] samples
  
```scala
import play.api.libs.functional.syntax._

// creates a Reads[ String :: Long :: (String :: Boolean :: HNil) :: HNil]
scala> val HListReads2 = (
     |    (__ \ "foo").read[String] and
     |    (__ \ "bar").read[Long] and
     |    (__ \ "toto").read(
     |      (
     |        (__ \ "alpha").read[String] and
     |        (__ \ "beta").read[Boolean]
     |      ).tupled.hlisted
     |    )
     | ).tupled.hlisted
HListReads2: play.api.libs.json.Reads[shapeless.::[String,shapeless.::[Long,shapeless.::[shapeless.::[String,shapeless.::[Boolean,shapeless.HNil]],shapeless.HNil]]]] = play.api.libs.json.Reads$$anon$8@7e4a09ee

// validates/converts JsObject to HList
scala> Json.obj(
     |   "foo" -> "toto", 
     |   "bar" -> 123L,
     |   "toto" -> Json.obj(
     |      "alpha" -> "chboing",
     |      "beta" -> true
     |   )
     | ).validate(HListReads2)
res7: play.api.libs.json.JsResult[shapeless.::[String,shapeless.::[Long,shapeless.::[shapeless.::[String,shapeless.::[Boolean,shapeless.HNil]],shapeless.HNil]]]] = 
JsSuccess(toto :: 123 :: chboing :: true :: HNil :: HNil,)

// Create a Writes[String :: Long :: HNil]
scala> implicit val HListWrites: Writes[ String :: Long :: HNil ] = (
     |         (__ \ "foo").write[String] and
     |         (__ \ "bar").write[Long]
     |       ).tupled.hlisted
HListWrites: play.api.libs.json.Writes[shapeless.::[String,shapeless.::[Long,shapeless.HNil]]] = play.api.libs.json.Writes$$anon$5@7c9d07e2

// writes a HList to JsValue
scala> Json.toJson("toto" :: 123L :: HNil)
res8: play.api.libs.json.JsValue = {"foo":"toto","bar":123}
```

## Adding shapelaysson in your dependencies

In your `Build.scala`, add:

```scala
import sbt._
import Keys._

object ApplicationBuild extends Build {

  val mandubianRepo = Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
  )

  val sonatypeRepo = Seq(
    "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"    
  )

  lazy val playJsonAlone = Project(
    BuildSettings.buildName, file("."),
    settings = BuildSettings.buildSettings ++ Seq(
      resolvers ++= mandubianRepo ++ sonatypeRepo,
      libraryDependencies ++= Seq(
        "org.mandubian"  %% "shapelaysson"  % "0.1-SNAPSHOT",
        "org.specs2"     %% "specs2"        % "1.13" % "test",
        "junit"           % "junit"         % "4.8" % "test"
      )
    )
  )
}

```

More to come maybe in this draft project...
Suggestions are welcome too

Have fun!
