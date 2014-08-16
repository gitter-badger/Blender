package transformations.javac.expressions.additive

import core.grammar._
import core.transformation._
import core.transformation.grammars.GrammarCatalogue
import core.transformation.sillyCodePieces.GrammarTransformation
import transformations.bytecode.coreInstructions.SubtractIntegerC
import transformations.javac.expressions.ExpressionC

object SubtractionC extends GrammarTransformation {
  val clazz: String = "Subtraction"

  val firstKey: String = "first"

  val secondKey: String = "second"

  override def inject(state: TransformationState): Unit = {
    ExpressionC.getExpressionToLines(state).put(clazz, subtraction => {
      val toInstructions = ExpressionC.getToInstructions(state)
      val firstInstructions = toInstructions(getFirst(subtraction))
      val secondInstructions = toInstructions(getSecond(subtraction))
      firstInstructions ++ secondInstructions ++ Seq(SubtractIntegerC.subtractInteger)
    })
  }

  def getFirst(subtraction: MetaObject) = subtraction(firstKey).asInstanceOf[MetaObject]

  def getSecond(subtraction: MetaObject) = subtraction(secondKey).asInstanceOf[MetaObject]

  override def dependencies: Set[Contract] = Set(AddAdditivePrecedence, SubtractIntegerC)

  override def transformGrammars(grammars: GrammarCatalogue) {
    val additiveGrammar = grammars.find(AddAdditivePrecedence.AdditiveExpressionGrammar)
    val parseSubtraction: Grammar = (additiveGrammar <~ "-") ~ additiveGrammar ^^ { case left ~ right => subtraction(left, right)}
    additiveGrammar.inner = additiveGrammar.inner | parseSubtraction
  }

  def subtraction(first: Any, second: Any): MetaObject = subtraction(first.asInstanceOf[MetaObject], second.asInstanceOf[MetaObject])

  def subtraction(first: MetaObject, second: MetaObject) = new MetaObject(clazz) {
    data.put(firstKey, first)
    data.put(secondKey, second)
  }
}
