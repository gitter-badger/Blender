package transformations.bytecode.instructions

import core.transformation.{MetaObject, TransformationState}
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.PrintByteCode._
import transformations.javac.base.ConstantPool
import transformations.javac.types.IntTypeC

object IntegerConstantC extends InstructionC {

  override val key: AnyRef = IntegerConstantKey

  def integerConstant(value: Int) = instruction(IntegerConstantKey, Seq(value))

  override def getInstructionStackSizeModification(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState): Int = 1

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    byteToBytes(3 + ByteCodeSkeleton.getIntegerConstantValue(instruction))
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject)
  = (Seq(), Seq(IntTypeC.intType))

  override def getInstructionSize: Int = 1

  object IntegerConstantKey

}
