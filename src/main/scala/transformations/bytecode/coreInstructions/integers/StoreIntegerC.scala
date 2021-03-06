package transformations.bytecode.coreInstructions.integers

import core.particles.node.{Key, Node}
import core.particles.{CompilationState, Contract}
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.coreInstructions.{InstructionC, InstructionSignature}
import transformations.bytecode.simpleBytecode.ProgramTypeState
import transformations.bytecode.types.IntTypeC

object StoreIntegerC extends InstructionC {

  override val key: Key = IntegerStore

  def integerStore(location: Int) = CodeAttribute.instruction(IntegerStore, Seq(location))

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = {
    val arguments = CodeAttribute.getInstructionArguments(instruction)
    val location = arguments(0)
    if (location > 3)
      hexToBytes("36") ++ byteToBytes(location)
    else
      byteToBytes(hexToInt("3b") + location)
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: CompilationState): InstructionSignature =
    InstructionSignature(Seq(IntTypeC.intType), Seq())

  override def getVariableUpdates(instruction: Node, typeState: ProgramTypeState ): Map[Int, Node] =
    Map(CodeAttribute.getInstructionArguments(instruction)(0) -> IntTypeC.intType)

  object IntegerStore extends Key

  override def dependencies: Set[Contract] = super.dependencies ++ Set(IntTypeC)

  override def description: String = "Defines the integer store instruction, which stores the top stack integer in a variable."
}
