package transformations.bytecode.coreInstructions.integers.integerCompare

import core.particles.{CompilationState, MetaObject}
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.coreInstructions.InstructionSignature
import transformations.bytecode.simpleBytecode.ProgramTypeState
import transformations.bytecode.{ByteCodeSkeleton, PrintByteCode}
import transformations.javac.classes.ConstantPool
import transformations.types.IntTypeC

object IfIntegerCompareNotEqualC extends JumpInstruction {

  override val key: AnyRef = IfIntegerCompareNotEqualKey

  def ifIntegerCompareGreater(target: Int): MetaObject = instruction(IfIntegerCompareNotEqualKey, Seq(target))

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    val arguments = ByteCodeSkeleton.getInstructionArguments(instruction)
    hexToBytes("a0") ++ shortToBytes(arguments(0))
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject, typeState: ProgramTypeState, state: CompilationState): InstructionSignature =
    InstructionSignature(Seq(IntTypeC.intType, IntTypeC.intType), Seq())

  object IfIntegerCompareNotEqualKey

}
