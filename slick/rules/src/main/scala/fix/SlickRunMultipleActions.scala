package fix

import scalafix.v1._

import scala.meta._

class SlickRunMultipleActions extends SemanticRule("SlickRunMultipleActions") {
  private def checkClass[T, U <: T](obj: T, cls: Class[_ <: U]): Option[U] =
    if (cls.isInstance(obj)) Some(cls.cast(obj)) else None

  private def simpleDealias(tpe: SemanticType)(implicit symtab: Symtab): SemanticType = {
    def dealiasSymbol(symbol: Symbol)(implicit symtab: Symtab): Symbol =
      symbol.info.get.signature match {
        case TypeSignature(_, lowerBound@TypeRef(_, dealiased, _), upperBound)
          if lowerBound == upperBound =>
          dealiased
        case _ =>
          symbol
      }

    tpe match {
      case TypeRef(prefix, symbol, typeArguments) =>
        TypeRef(prefix, dealiasSymbol(symbol), typeArguments.map(simpleDealias))
      case _ => throw new IllegalStateException(s"$tpe is not TypeRef, but ${tpe.getClass}")
    }
  }

  private def getParentSymbols(symbol: Symbol)(implicit symtab: Symtab): Set[Symbol] =
    symbol.info.get.signature match {
      case ClassSignature(_, parents, _, _) =>
        Set(symbol) ++ parents.collect {
          case TypeRef(_, symbol, _) => getParentSymbols(symbol)
        }.flatten
      case _ => Set.empty
    }

  private def isAnyOfStatementsDbIoAction(stats: Seq[Stat])(implicit symtab: Symtab, doc: SemanticDocument): Boolean =
    stats.size > 1 && stats.exists(statement =>
      (for {info <- statement.symbol.info
            methodSignature <- checkClass(info.signature, classOf[MethodSignature])
            returnType <- checkClass(methodSignature.returnType, classOf[TypeRef])
            dealiased <- checkClass(simpleDealias(returnType), classOf[TypeRef])
            parentSymbols = getParentSymbols(dealiased.symbol)
            } yield parentSymbols.exists(s => s.value == "slick/dbio/DBIOAction#")).getOrElse(false)
    )

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree
      .collect {
        case block@Term.Block(stats) if isAnyOfStatementsDbIoAction(stats) =>
          Seq(Patch.replaceToken(block.tokens.head, "("),
            Patch.addLeft(block, "DBIO.seq"),
            Patch.replaceToken(block.tokens.last, ")")) ++
            stats.map(Patch.addRight(_, ","))
      }
      .flatten
      .asPatch
  }
}
