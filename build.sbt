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
    // Compile / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
  )
)

publish / skip := true

lazy val rules = project.settings(
  moduleName := "customfixes",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val markers = project.settings()

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