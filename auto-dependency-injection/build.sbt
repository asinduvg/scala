ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "auto-dependency-injection"
  )

libraryDependencies ++= Seq(
  "org.tpolecat" %% "typename" % "1.0.0",
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.typelevel" %% "cats-effect" % "3.3.14",
  "org.http4s" %% "http4s-dsl" % "0.23.16",
  "dev.zio" %% "zio" % "2.1.6"
)
