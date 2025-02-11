package it.unibo.scafi.program

import it.unibo.scafi.test.AbstractScafiProgramTest

import scala.quoted.*

inline def listPrograms(): List[AbstractScafiProgramTest] = ${ listProgramsImpl() }

def listProgramsImpl()(using
    quotes: Quotes,
    tpe: Type[AbstractScafiProgramTest],
): Expr[List[AbstractScafiProgramTest]] =
  import quotes.reflect.*
  // get the current package
  val currentPackage = this.getClass.getPackageName
  // get symbols from string
  val packageSymbol = Symbol.requiredPackage(currentPackage)
  // packageSymbol
  val symbols: List[Symbol] = packageSymbol.declaredTypes

  val expressions = symbols
    .filter(_.isClassDef)
    .map(_.typeRef)
    .filter(_.<:<(TypeRepr.of[AbstractScafiProgramTest]))
    .map((repr: TypeRef) => repr.classSymbol.get)
    .map(method =>
      Apply(Select(New(TypeIdent(method)), method.primaryConstructor), List()).asExprOf[AbstractScafiProgramTest],
    )
    .toList
  Expr.ofList(expressions)
end listProgramsImpl
