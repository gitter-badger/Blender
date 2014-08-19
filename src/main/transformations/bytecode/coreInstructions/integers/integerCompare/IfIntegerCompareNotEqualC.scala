package transformations.bytecode.coreInstructions.integers.integerCompare

import core.transformation.{TransformationState, MetaObject}
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.PrintByteCode._
import transformations.javac.classes.ConstantPool
import transformations.types.IntTypeC

object IfIntegerCompareNotEqualC extends JumpInstruction {

  override val key: AnyRef = IfIntegerCompareNotEqualKey

  def ifIntegerCompareGreater(target: Int): MetaObject = instruction(IfIntegerCompareNotEqualKey, Seq(target))

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    val arguments = ByteCodeSkeleton.getInstructionArguments(instruction)
    hexToBytes("a0") ++ shortToBytes(arguments(0))
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState): (Seq[MetaObject], Seq[MetaObject]) =
    (Seq(IntTypeC.intType, IntTypeC.intType), Seq())

  object IfIntegerCompareNotEqualKey

}