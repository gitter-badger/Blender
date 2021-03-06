package transformations.javac.classes

import core.particles.grammars.GrammarCatalogue
import core.particles.{Contract, ParticleWithGrammar}
import transformations.javac.methods.VariableC
import transformations.javac.methods.VariableC.{VariableKey, VariableNameKey}

object ThisVariable extends ParticleWithGrammar
{
  object Grammar
  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val variable = grammars.find(VariableC.VariableGrammar)
    val thisGrammar = grammars.create(Grammar, ("this" ~> produce("this")) ^^ parseMap(VariableKey, VariableNameKey))
    variable.addOption(thisGrammar)
  }

  override def dependencies: Set[Contract] = Set(VariableC) ++ super.dependencies

  override def description: String = "Enables using the 'this' qualifier to refer to the current instance."
}
