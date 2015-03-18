package transformations.bytecode.coreInstructions

import core.particles.CompilationState
import core.particles.node.Node
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.constants.{ClassRefConstant, MethodDescriptorConstant, MethodRefConstant, NameAndType}
import transformations.bytecode.simpleBytecode.ProgramTypeState
import transformations.javac.classes.{ConstantPool, QualifiedClassName}
import transformations.types.{ObjectTypeC, TypeSkeleton}

abstract class InvokeC extends InstructionC {

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: CompilationState): InstructionSignature = {
    val constantPool: ConstantPool = ByteCodeSkeleton.getConstantPool(state)
    val methodRef = getInvokeTargetMethodRef(instruction, constantPool)
    val nameAndType = constantPool.getValue(MethodRefConstant.getMethodRefMethodNameIndex(methodRef)).asInstanceOf[Node]
    val descriptor = constantPool.getValue(NameAndType.getTypeIndex(nameAndType)).asInstanceOf[Node]
    getMethodStackModification(descriptor, constantPool, state)
  }

  def getInstanceInstructionSignature(instruction: Node, typeState: ProgramTypeState, state: CompilationState): InstructionSignature = {
    val constantPool = ByteCodeSkeleton.getConstantPool(state)
    val methodRef = getInvokeTargetMethodRef(instruction, constantPool)
    val nameAndType = constantPool.getValue(MethodRefConstant.getMethodRefMethodNameIndex(methodRef)).asInstanceOf[Node]
    val classRef = constantPool.getValue(MethodRefConstant.getMethodRefClassRefIndex(methodRef)).asInstanceOf[Node]
    val className = constantPool.getValue(ClassRefConstant.getNameIndex(classRef)).asInstanceOf[QualifiedClassName]
    val classType = ObjectTypeC.objectType(className)
    val descriptor = constantPool.getValue(NameAndType.getTypeIndex(nameAndType)).asInstanceOf[Node]
    val InstructionSignature(ins, outs) = getMethodStackModification(descriptor, constantPool, state)
    InstructionSignature(Seq(classType) ++ ins, outs)
  }

  def getMethodStackModification(descriptor: Node, constantPool: ConstantPool, state: CompilationState): InstructionSignature = {
    val ins = MethodDescriptorConstant.getMethodDescriptorParameters(descriptor).map(_type => TypeSkeleton.toStackType(_type, state))
    val outs = Seq(TypeSkeleton.toStackType(MethodDescriptorConstant.getMethodDescriptorReturnType(descriptor), state))
    InstructionSignature(ins, outs.filter(p => TypeSkeleton.getTypeSize(p, state) > 0))
  }

  def getInvokeTargetMethodRef(instruction: Node, constantPool: ConstantPool) = {
    val location = CodeAttribute.getInstructionArguments(instruction)(0)
    constantPool.getValue(location).asInstanceOf[Node]
  }

  override def getInstructionSize: Int = 3
}
