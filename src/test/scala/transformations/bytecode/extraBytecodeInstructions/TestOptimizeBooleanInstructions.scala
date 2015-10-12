package transformations.bytecode.extraBytecodeInstructions

import core.particles.CompilerFromParticles
import core.particles.node.Node
import org.junit.{Assert, Test}
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.extraBooleanInstructions.OptimizeBooleanInstructionsC
import transformations.bytecode.{ByteCodeMethodInfo, ByteCodeSkeleton}
import transformations.javac.JavaCompiler
import util.TestUtils

import scala.reflect.io.Path

class TestOptimizeBooleanInstructions {

  @Test
  def testForFibonacci() {
    val withOptimization = TestUtils.parseAndTransform("fibonacci", Path(""))
    val withoutOptimizationTransformations = JavaCompiler.javaCompilerTransformations.filter(i => i != OptimizeBooleanInstructionsC)
    val withoutOptimization = new TestUtils(new CompilerFromParticles(withoutOptimizationTransformations)).parseAndTransform("fibonacci", Path(""))

    val unoptimizedInstructions = getFibonacciInstructions(withoutOptimization)
    val optimizedInstructions = getFibonacciInstructions(withOptimization)

    Assert.assertTrue(s"optimizedInstructions.size (${optimizedInstructions.size}) + 5 < unoptimizedInstructions.size (${unoptimizedInstructions.size})",
      optimizedInstructions.size + 3 < unoptimizedInstructions.size)
  }

  def getFibonacciInstructions(clazz: Node) = {
    ByteCodeSkeleton.getMethods(clazz)
      .flatMap(methodInfo => ByteCodeMethodInfo.getMethodAttributes(methodInfo))
      .flatMap(annotation => if (annotation.clazz == CodeAttribute.CodeKey) Some(annotation) else None)
      .flatMap(codeAnnotation => CodeAttribute.getCodeInstructions(codeAnnotation))
  }
}
