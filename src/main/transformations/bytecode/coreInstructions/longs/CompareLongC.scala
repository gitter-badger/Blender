package transformations.bytecode.coreInstructions.longs

import core.transformation.{MetaObject, TransformationState}
import transformations.bytecode.PrintByteCode
import transformations.bytecode.coreInstructions.InstructionC
import transformations.javac.classes.ConstantPool
import transformations.types.{IntTypeC, LongTypeC}

object CompareLongC extends InstructionC {

  val compareLong = new MetaObject(CompareLongKey)

  object CompareLongKey

  override val key: AnyRef = CompareLongKey

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    PrintByteCode.hexToBytes("94")
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState) =
    (Seq(LongTypeC.longType, LongTypeC.longType), Seq(IntTypeC.intType))
}
