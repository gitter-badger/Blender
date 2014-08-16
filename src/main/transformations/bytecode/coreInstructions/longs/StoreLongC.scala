package transformations.bytecode.coreInstructions.longs

import core.transformation.{MetaObject, TransformationState}
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.coreInstructions.InstructionC
import transformations.javac.classes.ConstantPool
import transformations.types.LongTypeC

object StoreLongC  extends InstructionC {

  override val key: AnyRef = LongStore

  def longStore(location: Int) = instruction(LongStore, Seq(location))

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    val arguments = getInstructionArguments(instruction)
    val location = arguments(0)
    if (location > 3)
      hexToBytes("37") ++ byteToBytes(location)
    else
      byteToBytes(hexToInt("3f") + location)
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState)
  = (Seq(LongTypeC.longType), Seq())

  override def getVariableUpdates(instruction: MetaObject): Map[Int, MetaObject] =
    Map(getInstructionArguments(instruction)(0) -> LongTypeC.longType)

  object LongStore

}