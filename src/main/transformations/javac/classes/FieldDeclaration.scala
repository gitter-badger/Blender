package transformations.javac.classes

import core.particles.grammars.GrammarCatalogue
import core.particles.node.Node
import core.particles.{CompilationState, Contract, ParticleWithGrammar}
import transformations.bytecode.constants.FieldDescriptorConstant
import transformations.bytecode.{ByteCodeFieldInfo, ByteCodeSkeleton}
import transformations.types.TypeSkeleton

object FieldDeclaration extends ParticleWithGrammar {

  object FieldKey
  object FieldType
  object FieldName

  override def dependencies: Set[Contract] = super.dependencies ++ Set(JavaClassSkeleton, FieldDescriptorConstant)

  override def inject(state: CompilationState): Unit = {
    super.inject(state)

    JavaClassSkeleton.getState(state).firstMemberPasses ::= (clazz => bindFields(state, clazz))
    JavaClassSkeleton.getState(state).secondMemberPasses ::= (clazz => convertFields(state, clazz))
  }
  
  def bindFields(state: CompilationState, clazz: Node): Unit = {
    val classCompiler = JavaClassSkeleton.getClassCompiler(state)
    val classInfo = classCompiler.currentClassInfo

    val fields = getFields(clazz)
    for (field <- fields)
      bindField(field)

    def bindField(field: Node) = {
      val name: String = getFieldName(field)
      val _type = getFieldType(field)
      classInfo.newFieldInfo(name, _type)
    }
  }

  def getFieldType(field: Node): Node = {
    field(FieldType).asInstanceOf[Node]
  }

  def getFieldName(field: Node): String = {
    field(FieldName).asInstanceOf[String]
  }

  def getFields(clazz: Node): Seq[Node] = {
    JavaClassSkeleton.getMembers(clazz).filter(member => member.clazz == FieldKey)
  }

  def convertFields(state: CompilationState, clazz: Node) = {
    val classCompiler = JavaClassSkeleton.getClassCompiler(state)

    val fields = getFields(clazz)
    clazz(ByteCodeSkeleton.ClassFields) = fields.map(field => {
      convertField(field, classCompiler, state)
      field
    })
  }
  
  def convertField(field: Node, classCompiler: ClassCompiler, state: CompilationState) {
    val constantPool = classCompiler.constantPool
    val nameIndex = classCompiler.getNameIndex(getFieldName(field))

    field(ByteCodeFieldInfo.NameIndex) = nameIndex
    field.clazz = ByteCodeFieldInfo.FieldKey

    val fieldDescriptorIndex = constantPool.store(FieldDescriptorConstant.constructor(getFieldType(field)))
    field(ByteCodeFieldInfo.DescriptorIndex) = fieldDescriptorIndex
    field(ByteCodeFieldInfo.AccessFlagsKey) = Set.empty
    field(ByteCodeFieldInfo.FieldAttributes) = Seq.empty

    field.data.remove(FieldName)
    field.data.remove(FieldType)
  }

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val memberGrammar = grammars.find(JavaClassSkeleton.ClassMemberGrammar)
    val typeGrammar = grammars.find(TypeSkeleton.TypeGrammar)

    val fieldGrammar = typeGrammar ~~ identifier <~ ";" ^^ parseMap(FieldKey, FieldType, FieldName)
    memberGrammar.addOption(fieldGrammar)
  }

  override def description: String = "Enables adding a field declaration without an initializer to a Java class."
}
