package transformations.javac.statements

import core.transformation.grammars.GrammarCatalogue
import core.transformation.{Contract, MetaObject, TransformationState}
import transformations.bytecode.LabelledTargets
import transformations.bytecode.simpleBytecode.InferredStackFrames
import transformations.javac.expressions.ExpressionC
import core.grammar.~

object IfThenC extends StatementInstance {
  object IfThenKey
  object ConditionKey
  object ThenKey

  override val key: AnyRef = IfThenKey

  override def dependencies: Set[Contract] = super.dependencies ++ Set(BlockC)

  override def toByteCode(ifThen: MetaObject, state: TransformationState): Seq[MetaObject] = {
    val condition = ifThen(ConditionKey).asInstanceOf[MetaObject]
    val endLabelName = state.getUniqueLabel("end")
    val end = InferredStackFrames.label(endLabelName)
    val body = ifThen(ThenKey).asInstanceOf[Seq[MetaObject]]

    val conditionalBranch = LabelledTargets.ifZero(endLabelName)
    val toInstructionsExpr = ExpressionC.getToInstructions(state)
    val toInstructionsStatement = StatementC.getToInstructions(state)
    toInstructionsExpr(condition) ++
      Seq(conditionalBranch) ++
      body.flatMap(toInstructionsStatement) ++
      Seq(end)
  }

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val statementGrammar = grammars.find(StatementC.StatementGrammar)
    val expressionGrammar = grammars.find(ExpressionC.ExpressionGrammar)
    val blockGrammar = grammars.find(BlockC.BlockGrammar)
    val ifThenGrammar = "if" ~> ("(" ~> expressionGrammar <~ ")") ~ blockGrammar ^^
      { case condition ~ body => new MetaObject(IfThenKey, ConditionKey -> condition, ThenKey -> body)}
    statementGrammar.orToInner(ifThenGrammar)
  }
}
