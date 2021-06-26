lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    organization := "com.github.aborg0",
    homepage := Some(url("https://github.com/aborg0/customfixes")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "aborg0",
        "Gabor Bakos",
        "bakos.gabor@mind-era.com",
        url("https://mind-era.com")
      )
    ),
    scalaVersion := V.scala213,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos"
    ),
    version := "0.0.1-SNAPSHOT",
    // Compile / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
  )
)

publish / skip := true

lazy val rules = project.settings(
  moduleName := "customfixes",
  crossScalaVersions := List(V.scala213, V.scala211, V.scala212),
  publish / scalaVersion := "2.12.14",
  publishLocal / scalaVersion := "2.12.14",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val markers = project.settings(
  moduleName := "customfixes-markers",
)

lazy val input = project.dependsOn(markers).settings(
  publish / skip := true
)

lazy val output = project.dependsOn(markers).settings(
  publish / skip := true
)

lazy val tests = project
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := (output / Compile / sourceDirectories).value,
    scalafixTestkitInputSourceDirectories := (input / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath := (input / Compile / fullClasspath).value
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)