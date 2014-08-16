package application.graphing.model.simplifications

import core.transformation.Contract
import transformations.javac.methods.MethodC
import transformations.javac.methods.assignment.AssignmentPrecedence
import transformations.javac.statements._

object JavaSimpleStatement extends TransformationGroup {

  override def dependencies: Set[Contract] = Set(DeclarationC, IfThenC, ExpressionAsStatementC, ForLoopC)

  override def dependants: Set[Contract] = Set(MethodC, AssignmentPrecedence)
}
