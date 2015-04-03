package transformations.types

import core.bigrammar.BiGrammar
import core.grammar.FailureG
import core.particles.ParticleWithGrammar
import core.particles.grammars.GrammarCatalogue
import core.particles.node.Node

object TypeAbstraction extends ParticleWithGrammar {

  object TypeAbstractionKey
  object Body
  object Parameters
  object ParametersKey
  object ParameterName
  object ParameterBound

  def getBody(_type: Node): Node = {
    _type(TypeAbstraction.Body).asInstanceOf[Node]
  }

  def getParameters(_type: Node): Seq[Node] = {
    _type(TypeAbstraction.Parameters).asInstanceOf[Seq[Node]]
  }

  object TypeParametersGrammar
  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    transformByteCodeGrammar(grammars)
    val variableGrammar: BiGrammar = new FailureG()
    val parametersGrammar: BiGrammar = variableGrammar.some
    grammars.create(TypeParametersGrammar, ("<" ~> parametersGrammar <~ ">").option.optionToSeq)
  }

  def transformByteCodeGrammar(grammars: GrammarCatalogue): Unit = {
    val byteCodeType = grammars.find(TypeSkeleton.ByteCodeTypeGrammar)
    val methodTypeGrammar = grammars.find(MethodTypeC.ByteCodeMethodTypeGrammar)
    val objectTypeGrammar = grammars.find(ObjectTypeC.ObjectTypeByteCodeGrammar)
    val classBound: BiGrammar = objectTypeGrammar
    val variableGrammar: BiGrammar = identifier ~ (":" ~> classBound) ^^ parseMap(ParametersKey, ParameterName, ParameterBound)
    val parametersGrammar: BiGrammar = variableGrammar.some
    val abstractMethodType = ("<" ~> parametersGrammar <~ ">") ~ methodTypeGrammar ^^
      parseMap(TypeAbstractionKey, Parameters, Body)
    byteCodeType.addOption(abstractMethodType)
  }

  override def description: String = "Adds type abstraction or 'generics'."
}
