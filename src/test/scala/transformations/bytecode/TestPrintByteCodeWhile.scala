package transformations.bytecode

import core.particles.node.Node
import org.junit.{Assert, Test}
import org.scalatest.FunSuite
import transformations.bytecode.attributes._
import transformations.bytecode.constants.{ClassRefConstant, MethodRefConstant, NameAndType}
import transformations.bytecode.coreInstructions._
import transformations.bytecode.coreInstructions.integers.integerCompare.IfIntegerCompareGreaterOrEqualC
import transformations.bytecode.coreInstructions.integers.{IncrementIntegerC, LoadIntegerC, SmallIntegerConstantC, StoreIntegerC}
import transformations.bytecode.coreInstructions.objects.LoadAddressC
import transformations.bytecode.types.{IntTypeC, VoidTypeC}
import transformations.javac.classes.ConstantPool
import transformations.javac.classes.skeleton.QualifiedClassName
import transformations.javac.types.MethodTypeC
import util.TestUtils

class TestPrintByteCodeWhile extends FunSuite {

  test("PrintByteCode") {
    val expectedHex = "cafe babe 0000 0033 000f 0a00 0300 0c07\n000d 0700 0e01 0006 3c69 6e69 743e 0100\n0328 2956 0100 0443 6f64 6501 000f 4c69\n6e65 4e75 6d62 6572 5461 626c 6501 0006\n7768 696c 6565 0100 0d53 7461 636b 4d61\n7054 6162 6c65 0100 0a53 6f75 7263 6546\n696c 6501 000b 5768 696c 6565 2e6a 6176\n610c 0004 0005 0100 216c 616e 6775 6167\n6573 2f62 7974 6563 6f64 652f 7465 7374\n696e 672f 5768 696c 6565 0100 106a 6176\n612f 6c61 6e67 2f4f 626a 6563 7400 2000\n0200 0300 0000 0000 0200 0000 0400 0500\n0100 0600 0000 1d00 0100 0100 0000 052a\nb700 01b1 0000 0001 0007 0000 0006 0001\n0000 0003 0009 0008 0005 0001 0006 0000\n003f 0002 0001 0000 000e 033b 1a06 a200\n0984 0001 a7ff f8b1 0000 0002 0007 0000\n0012 0004 0000 0005 0002 0006 0007 0008\n000d 000a 0009 0000 0007 0002 fc00 0201\n0a00 0100 0a00 0000 0200 0b"
    assertResult(expectedHex)(TestUtils.printByteCode(getByteCode))
  }

  def getByteCode: Node = {
    val constantPool = new ConstantPool(Seq(MethodRefConstant.methodRef(3, 12),
      ClassRefConstant.classRef(13),
      ClassRefConstant.classRef(14),
      "<init>",
      MethodTypeC.construct(VoidTypeC.voidType, Seq()),
      CodeConstantEntry.entry,
      LineNumberTable.constantPoolKey,
      "whilee",
      StackMapTableAttribute.stackMapTableId,
      SourceFileAttribute.constantPoolKey,
      "Whilee.java",
      NameAndType.nameAndType(4, 5),
      new QualifiedClassName(Seq("languages", "bytecode", "testing", "Whilee")),
      new QualifiedClassName(Seq("java", "lang", "Object"))))
    val constructor: Node = getConstructor
    val _while: Node = getWhile
    val methods = Seq(constructor, _while)
    val classAttributes = Seq(SourceFileAttribute.sourceFile(10, 11))
    ByteCodeSkeleton.clazz(2, 3, constantPool, methods, attributes = classAttributes)
  }

  def getConstructor: Node = {
    val lineNumberTable = LineNumberTable.lineNumberTable(7, Seq(new LineNumberRef(3, 0)))
    val constructor = ByteCodeMethodInfo.methodInfo(4, 5, Seq(
      CodeAttribute.codeAttribute(6, 1, 1, Seq(
        LoadAddressC.addressLoad(0),
        InvokeSpecialC.invokeSpecial(1),
        VoidReturnInstructionC.voidReturn
      ), Seq(), Seq(lineNumberTable))))
    constructor
  }

  def getWhile: Node = {
    val lineNumberTable = LineNumberTable.lineNumberTable(7, Seq(
      new LineNumberRef(5, 0),
      new LineNumberRef(6, 2),
      new LineNumberRef(8, 7),
      new LineNumberRef(10, 13)
    ))
    val stackMapTable = StackMapTableAttribute.stackMapTable(9, Seq(StackMapTableAttribute.appendFrame(2, Seq(IntTypeC.intType)),
      StackMapTableAttribute.sameFrame(10)))
    val _while = ByteCodeMethodInfo.methodInfo(8, 5, Seq(CodeAttribute.codeAttribute(6, 2, 1, Seq(
      SmallIntegerConstantC.integerConstant(0),
      StoreIntegerC.integerStore(0),
      LoadIntegerC.load(0),
      SmallIntegerConstantC.integerConstant(3),
      IfIntegerCompareGreaterOrEqualC.ifIntegerCompareGreater(9),
      IncrementIntegerC.integerIncrement(0, 1),
      GotoC.goTo(-8),
      VoidReturnInstructionC.voidReturn
    ), Seq(), Seq(lineNumberTable, stackMapTable))), Set(ByteCodeMethodInfo.PublicAccess, ByteCodeMethodInfo.StaticAccess))
    _while
  }
}
