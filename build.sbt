name := "playLaserArchive"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe.slick" %% "slick" % "2.0.1"
)     

play.Project.playScalaSettings

lessEntryPoints <<= baseDirectory(_ / "app" / "assets" / "stylesheets" ** "main.less")