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
