import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName = "lemme"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    filters,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "com.typesafe.play" %% "play-slick" % "0.5.0.3-SNAPSHOT",
    "com.github.tototoshi" %% "slick-joda-mapper" % "0.3.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases",
      "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "java-net" at "http://download.java.net/maven/2",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      Resolver.url("My GitHub Play Repository", url("http://krzysztofkowalski.github.io/releases/"))(Resolver.ivyStylePatterns),
      // maven repo
      Resolver.url("maven.org", url("http://repo1.maven.org/maven2/"))(Resolver.ivyStylePatterns),
      // secure social
      Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )
  ).dependsOn(RootProject(uri("git://github.com/freekh/play-slick.git")))
}
