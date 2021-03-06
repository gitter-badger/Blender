package transformations.javac.expressions.additive

import core.particles._
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Key, Node, NodeLike}
import core.particles.path.Path
import transformations.bytecode.coreInstructions.integers.SubtractIntegerC
import transformations.bytecode.types.{IntTypeC, TypeSkeleton}
import transformations.javac.expressions.{ExpressionInstance, ExpressionSkeleton}

object SubtractionC extends ExpressionInstance {
  object SubtractionKey extends Key
  object FirstKey
  object SecondKey

  def getFirst[T <: NodeLike](subtraction: T) = subtraction(FirstKey).asInstanceOf[T]

  def getSecond[T <: NodeLike](subtraction: T) = subtraction(SecondKey).asInstanceOf[T]

  override def dependencies: Set[Contract] = Set(AddAdditivePrecedence, SubtractIntegerC)

  override def transformGrammars(grammars: GrammarCatalogue) {
    val additiveGrammar = grammars.find(AddAdditivePrecedence.AdditiveExpressionGrammar)
    val withoutSubtraction = additiveGrammar.inner //We're doing this to get "-" to behave right associative. Hope this doesn't have any bad side-effects.
    val parseSubtraction = (additiveGrammar <~~ "-") ~~ withoutSubtraction ^^ parseMap(SubtractionKey, FirstKey, SecondKey)
    additiveGrammar.addOption(parseSubtraction)
  }

  def subtraction(first: Any, second: Any): Node = subtraction(first.asInstanceOf[Node], second.asInstanceOf[Node])

  def subtraction(first: Node, second: Node) = new Node(SubtractionKey,
    FirstKey -> first,
    SecondKey -> second)

  override val key: Key = SubtractionKey

  override def getType(expression: Path, state: CompilationState): Node = {
    val getType = ExpressionSkeleton.getType(state)
    val firstType = getType(getFirst(expression))
    val secondType = getType(getSecond(expression))
    TypeSkeleton.checkAssignableTo(state)(IntTypeC.intType, firstType)
    TypeSkeleton.checkAssignableTo(state)(IntTypeC.intType, secondType)
    IntTypeC.intType
  }

  override def toByteCode(subtraction: Path, state: CompilationState): Seq[Node] = {
    val toInstructions = ExpressionSkeleton.getToInstructions(state)
    val firstInstructions = toInstructions(getFirst(subtraction))
    val secondInstructions = toInstructions(getSecond(subtraction))
    firstInstructions ++ secondInstructions ++ Seq(SubtractIntegerC.subtractInteger)
  }

  override def description: String = "Adds the - operator."
}
