/*
rule = CaseClassArgumentNameMatch
 */
package test

object CaseClassArgumentNameMatch {
  import com.github.aborg0.customfixes.marker.NamesShouldMatch

  final case class Person(lastName: String, firstName: String, age: Int, profession: Set[String])
    extends NamesShouldMatch

  final case class PersonUnchecked(lastName: String, firstName: String)

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
    (null: Person) match {
      case person@Person(firstName, _, _, _) => () // assert: CaseClassArgumentNameMatch
    }
    (null: Option[Person]) match {
      case Some(person@Person(firstName, _, _, _)) => () // assert: CaseClassArgumentNameMatch
    }
    (null: Either[String, Person]) match {
      case Left(message) => ()
      case Right(person@Person(firstName, _, _, _)) => () // assert: CaseClassArgumentNameMatch
    }
    // This might be questionable, but for now it still reports a warning
    (null: Person) match {
      case person@Person(firstName@"S", _, _, _) => () // assert: CaseClassArgumentNameMatch
    }
    (null: PersonUnchecked) match {
      case PersonUnchecked(firstName, lastName) => () // ok, it is not extending the marker trait
    }
  }
}
