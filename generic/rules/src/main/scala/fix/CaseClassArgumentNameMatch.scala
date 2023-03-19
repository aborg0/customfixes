package fix

import scalafix.v1._

import scala.meta._

case class LiteralArgument(
                            position: Position,
                            parameterName: String,
                            literal: String
                          ) extends Diagnostic {
  override def message: String =
    s"Inconsistent name for '$parameterName'"
}

class CaseClassArgumentNameMatch extends SemanticRule("CaseClassArgumentNameMatch") {
  private def checkClass[T, U <: T](obj: T, cls: Class[_ <: U]): Option[U] =
    if (cls.isInstance(obj)) Some(cls.cast(obj)) else None

  private def hasNamesShouldMatch(typeRefs: Seq[TypeRef])(implicit symtab: Symtab): Boolean = typeRefs
    .exists(ref => ref.symbol.value == "com/github/aborg0/customfixes/marker/NamesShouldMatch#" ||
      ref.symbol.info.flatMap(info => Some(info.signature).collect {
        case cs: ClassSignature => hasNamesShouldMatch(cs.parents.collect { case v: TypeRef => v })
      }).getOrElse(false))

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree
      .collect {
        case Pat.Extract((term, args)) =>
          (for { // Option for comprehension, hence the .toSeq.flatten at the end
            companion <- Symbol(term.symbol.value.init + "#").info
            companionSignature <- checkClass(companion.signature, classOf[ClassSignature])
            if hasNamesShouldMatch(companionSignature.parents.collect{case tr: TypeRef => tr})
            termSymbolInfo <- term.symbol.info
            sig <- checkClass(termSymbolInfo.signature, classOf[ClassSignature])
            applyInfo <- sig.declarations.find(_.symbol.value.endsWith(".apply()."))
            applyMethodSignature <- checkClass(applyInfo.signature, classOf[MethodSignature])
            names <- applyMethodSignature.parameterLists.headOption.map(sis => sis.map(_.displayName))
          } yield {
            args.zip(names).collect {
              case (v@Pat.Var(name), expected) if name.value != expected =>
                Patch.lint(LiteralArgument(v.pos, expected, v.syntax))
              case (v@Pat.Bind(Pat.Var(name), _), expected) if name.value != expected =>
                Patch.lint(LiteralArgument(v.pos, expected, v.syntax))
            }
          }).toSeq.flatten
      }
      .flatten
      .asPatch
  }

}