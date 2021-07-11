# Custom scalafix rules

This project aims to find certain problems in Scala code, like
 - using different name for case class pattern match as it was used in the constructor
   (only when case class was declared with a marker trait (`NamesShouldMatch`))
   
Currently we do not have a release yet. In order to use this in your project, please do the following:
 - check out this project
   - `sbt markers/publishLocal`
   - `sbt "++ 2.12.14 rules/publishLocal"`
 - add the generated artifacts as a dependency: `libraryDependencies += "com.github.aborg0" %% "customfixes-generic-markers" % "0.0.1-SNAPSHOT"`
  - tip: In case you have issues with IDEA not being able to import your project 
    ("Extracting structure failed, reason: not ok build status: Error (BuildMessages(Vector(),Vector(),Vector(),Vector(),Error))"), 
    stop the sbt shell first!
  - and as a [scalafix dependency](https://scalacenter.github.io/scalafix/docs/rules/external-rules.html): `ThisBuild / scalafixDependencies += "com.github.aborg0" %% "customfixes-generic" % "0.0.1-SNAPSHOT"`
 - the `project/plugins.sbt` should contain `addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.29")`

## Case class argument name match

In case you have a case class like this in your codebase:
```scala
import com.github.aborg0.customfixes.marker.NamesShouldMatch

final case class Person(lastName: String, firstName: String, age: Int) extends NamesShouldMatch
```
And other part contains something like:
```scala
  val somebody = Person(lastName= "lastName", firstName = "firstName", age = 11)

  somebody match {
    case Person(firstName, _, _) => println(s"Should warn")
  }
```
(Note that the `firstName` and `lastName` are in different order!)

From the sbt shell the `scalafix CaseClassArgumentNameMatch` should report something similar:
```
[info] Running scalafix on 2 Scala sources
[error] C:\Users\demo\IdeaProjects\customfixes_example\src\main\scala\org\example\Main.scala:10:17: error: [CaseClassArgumentNameMatch] Inconsistent name for 'lastName'
[error]     case Person(firstName, _, _) => println(s"Should warn")
[error]                 ^^^^^^^^^
[error] (Compile / scalafix) scalafix.sbt.ScalafixFailed: LinterError
```
