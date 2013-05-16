import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "photoploke"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.mongodb" %% "casbah" % "2.5.0",
    "com.kitfox.svg" % "svg-salamander" % "1.0",
    "batik" % "batik-transcoder" % "1.6-1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here

  )

}
