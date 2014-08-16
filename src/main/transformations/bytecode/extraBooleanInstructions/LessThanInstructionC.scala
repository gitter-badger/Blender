package transformations.bytecode.extraBooleanInstructions

import core.transformation.{Contract, MetaObject, TransformationState}
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.LabelledTargets
import transformations.bytecode.coreInstructions.integers.IntegerConstantC
import transformations.bytecode.coreInstructions.integers.integerCompare.IfIntegerCompareLessC
import transformations.bytecode.simpleBytecode.InferredStackFrames

object LessThanInstructionC extends ExpandInstruction {

  def lessThanInstruction = instruction(LessThanInstructionKey)

  override def dependencies: Set[Contract] = Set(LabelledTargets, IfIntegerCompareLessC)

  override def key: Any = LessThanInstructionKey

  override def expand(instruction: MetaObject, state: TransformationState): Seq[MetaObject] = {
    val falseStartLabel = state.getUniqueLabel("falseStart")
    val endLabel = state.getUniqueLabel("end")
    Seq(LabelledTargets.ifIntegerCompareLess(falseStartLabel),
      IntegerConstantC.integerConstant(0),
      LabelledTargets.goTo(endLabel),
      InferredStackFrames.label(falseStartLabel),
      IntegerConstantC.integerConstant(1),
      InferredStackFrames.label(endLabel))
  }

  object LessThanInstructionKey

}
