import sbt._
import Keys._

object BuildSettings {
  val buildName              = "shapelaysson"
  val buildOrganization      = "org.mandubian"

  val buildScalaVersion      = "2.10.0"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    scalaVersion    := buildScalaVersion,
    organization    := buildOrganization
  )
}

object ApplicationBuild extends Build {

  val mandubianRepo = Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
  )

  val sonatypeRepo = Seq(
    "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"    
  )

  lazy val shapelaysson = Project(
    BuildSettings.buildName, file("."),
    settings = BuildSettings.buildSettings ++ Seq(
      resolvers ++= mandubianRepo ++ sonatypeRepo,
      libraryDependencies ++= Seq(
        "play"        %% "play-json" % "2.2-SNAPSHOT",
        "com.chuusai"  % "shapeless_2.10.0" % "1.2.4",
        "org.specs2"  %% "specs2" % "1.13" % "test",
        "junit"        % "junit" % "4.8" % "test"
      ),
      publishMavenStyle := true,
      publishTo <<= version { (version: String) =>
        val localPublishRepo = "../mandubian-mvn/"
        if(version.trim.endsWith("SNAPSHOT"))
          Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
        else Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
      }
    )
  )
}
