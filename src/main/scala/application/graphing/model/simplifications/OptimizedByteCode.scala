package application.graphing.model.simplifications

import core.particles.Contract
import transformations.bytecode.additions.PoptimizeC
import transformations.bytecode.extraBooleanInstructions.OptimizeComparisonInstructionsC
import transformations.javac.expressions.ExpressionSkeleton

object OptimizedByteCode extends TransformationGroup {
  override def dependants: Set[Contract] = Set(ExpressionSkeleton)

  override def dependencies: Set[Contract] = Set(OptimizeComparisonInstructionsC, PoptimizeC)
}
