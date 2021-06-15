/*
rule = CaseClassArgumentNameMatch
 */
package test

object CaseClassArgumentNameMatch {
  import com.github.aborg0.customfixes.marker.NamesShouldMatch

  final case class Person(lastName: String, firstName: String, age: Int, profession: Set[String])
    extends NamesShouldMatch

  def simple: Unit = {
    (null: Person) match {
      case Person(firstName, lastName, age, profession) => () // assert: CaseClassArgumentNameMatch
    }
    (null: Person) match {
      case Person(_, lastName, _, _) => () /* assert: CaseClassArgumentNameMatch
                     ^^^^^^^^
      Inconsistent name for 'firstName'
      */
    }
    (null: Person) match {
      case Person(lastName, firstName, _, _) => ()
    }
    (null: Person) match {
      case Person(_, firstName, _, _) => ()
    }
  }
}
