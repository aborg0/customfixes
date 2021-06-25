package fix

import scalafix.v1._

import scala.meta._
import scala.meta.internal.semanticdb.SymbolInformation

case class LiteralArgument(
                            position: Position,
                            parameterName: String,
                            literal: String
                          ) extends Diagnostic {
  override def message: String =
    s"Inconsistent name for '$parameterName'"
}

class CaseClassArgumentNameMatch extends SemanticRule("CaseClassArgumentNameMatch") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree
      .collect {
        case Pat.Extract((term, args)) =>
          val sig@ClassSignature(typeParameters, parents, self, declarations) = term.symbol.info.get.signature
          val Some(appl) = declarations.find(_.symbol.value.endsWith(".apply()."))
          val MethodSignature(_, List(params, _*), _) = appl.signature
          val names = params.map(si => si.symbol.value.substring(si.symbol.value.lastIndexOf('(') + 1, si.symbol.value.length - 1))
          val companion = Symbol(term.symbol.value.init + "#")
          val ClassSignature(_, compParents, _, _) = companion.info.get.signature
          def hasNamesShouldMatch(typeRefs: Seq[TypeRef]): Boolean = typeRefs
            .exists(ref => ref.symbol.value == "com/github/aborg0/customfixes/marker/NamesShouldMatch#" || ref.symbol.info.flatMap(info => Some(info.signature).collect{
              case cs:ClassSignature => hasNamesShouldMatch(cs.parents.collect{case v: TypeRef => v})
            }).getOrElse(false))
          val parentsContainsMarker = hasNamesShouldMatch(compParents.collect { case v:TypeRef => v})
          args.zip(names).collect {
            case (v@Pat.Var(name), expected) if parentsContainsMarker && name.value != expected =>
              Patch.lint(LiteralArgument(v.pos, expected, v.syntax))
          }
      }
      .flatten
      .asPatch
  }

}