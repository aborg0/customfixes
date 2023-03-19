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

lazy val genericRules = project.in(file("generic/rules")).settings(
  moduleName := "customfixes-generic",
  crossScalaVersions := List(V.scala213, V.scala211, V.scala212),
  publish / scalaVersion := "2.12.17",
  publishLocal / scalaVersion := "2.12.17",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val genericMarkers = project.in(file("generic/markers")).settings(
  moduleName := "customfixes-generic-markers",
)

lazy val genericInput = project.in(file("generic/input")).dependsOn(genericMarkers).settings(
  moduleName := "generic-input",
  publish / skip := true
)

lazy val genericOutput = project.in(file("generic/output")).dependsOn(genericMarkers).settings(
  moduleName := "generic-output",
  publish / skip := true
)

lazy val genericTests = project.in(file("generic/tests"))
  .settings(
    moduleName := "generic-tests",
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := (genericOutput / Compile / sourceDirectories).value,
    scalafixTestkitInputSourceDirectories := (genericInput / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath := (genericInput / Compile / fullClasspath).value
  )
  .dependsOn(genericRules)
  .enablePlugins(ScalafixTestkitPlugin)

lazy val slickRules = project.in(file("slick/rules")).settings(
  moduleName := "customfixes-slick",
  crossScalaVersions := List(V.scala213, V.scala211, V.scala212),
  publish / scalaVersion := "2.12.14",
  publishLocal / scalaVersion := "2.12.14",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val slickInput = project.in(file("slick/input")).settings(
  moduleName := "slick-input",
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.3.3",
    "org.slf4j" % "slf4j-nop" % "2.0.5",
  ),
  publish / skip := true
)

lazy val slickOutput = project.in(file("slick/output")).settings(
  moduleName := "slick-output",
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.3.3",
    "org.slf4j" % "slf4j-nop" % "2.0.5",
  ),
  publish / skip := true
)

lazy val slickTests = project.in(file("slick/tests"))
  .settings(
    moduleName := "slick-tests",
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories := (slickOutput / Compile / sourceDirectories).value,
    scalafixTestkitInputSourceDirectories := (slickInput / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath := (slickInput / Compile / fullClasspath).value
  )
  .dependsOn(slickRules)
  .enablePlugins(ScalafixTestkitPlugin)
