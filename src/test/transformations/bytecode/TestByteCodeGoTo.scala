package transformations.bytecode

import core.particles.CompilerFromParticles
import core.particles.node.Node
import org.junit.Test
import transformations.bytecode.additions.LabelledTargets
import transformations.bytecode.attributes.{CodeAttribute, StackMapTableAttribute}
import transformations.bytecode.coreInstructions._
import transformations.bytecode.coreInstructions.integers.integerCompare.IfIntegerCompareGreaterOrEqualC
import transformations.bytecode.coreInstructions.integers.{IncrementIntegerC, IntegerConstantC, LoadIntegerC, StoreIntegerC}
import transformations.javac.JavaCompiler
import transformations.javac.classes.ConstantPool
import transformations.bytecode.types.IntTypeC
import util.TestUtils

class TestByteCodeGoTo {

  def testMain(instructions: Seq[Node]): Node = {
    val method = ByteCodeMethodInfo.methodInfo(0, 0, Seq(CodeAttribute.codeAttribute(0, 0, 0, instructions, Seq(), Seq())))
    ByteCodeSkeleton.clazz(2, 3, new ConstantPool(), Seq(method))
  }

  @Test
  def compareCompiledVersusNativeCode() {
    val labelledWhile = getLabelledJumpWhile
    val compiledWhile = new CompilerFromParticles(Seq(LabelledTargets) ++ JavaCompiler.byteCodeTransformations).transform(labelledWhile)
    val expectedCode = getExpectedJumpWhile
    TestUtils.testInstructionEquivalence(compiledWhile, expectedCode)
  }

  def getExpectedJumpWhile: Node = {
    val instructions = Seq(
      IntegerConstantC.integerConstant(0),
      StoreIntegerC.integerStore(0),
      LoadIntegerC.load(0),
      IntegerConstantC.integerConstant(3),
      IfIntegerCompareGreaterOrEqualC.ifIntegerCompareGreater(9),
      IncrementIntegerC.integerIncrement(0, 1),
      GotoC.goTo(-8))

    val stackMapTable = StackMapTableAttribute.stackMapTable(1, Seq(StackMapTableAttribute.appendFrame(2, Seq(IntTypeC.intType)),
      StackMapTableAttribute.sameFrame(10)))
    val method = ByteCodeMethodInfo.methodInfo(0, 0, Seq(CodeAttribute.codeAttribute(0, 0, 0, instructions, Seq(), Seq(stackMapTable))))
    ByteCodeSkeleton.clazz(2, 3, new ConstantPool(Seq(StackMapTableAttribute.stackMapTableId)), Seq(method))
  }

  def getLabelledJumpWhile: Node = {
    val instructions = Seq(
      IntegerConstantC.integerConstant(0),
      StoreIntegerC.integerStore(0),
      LabelledTargets.label("start", new Node(StackMapTableAttribute.AppendFrame,
        StackMapTableAttribute.AppendFrameTypes -> Seq(IntTypeC.intType))),
      LoadIntegerC.load(0),
      IntegerConstantC.integerConstant(3),
      LabelledTargets.ifIntegerCompareGreaterEquals("end"),
      IncrementIntegerC.integerIncrement(0, 1),
      LabelledTargets.goTo("start"),
      LabelledTargets.label("end", new Node(StackMapTableAttribute.SameFrameKey))
    )

    val method = ByteCodeMethodInfo.methodInfo(0, 0, Seq(CodeAttribute.codeAttribute(0, 0, 0, instructions, Seq(), Seq())))
    ByteCodeSkeleton.clazz(2, 3, new ConstantPool(), Seq(method))
  }
}
