package transformations.javac.methods

import core.grammar.seqr
import core.transformation.grammars.GrammarCatalogue
import core.transformation.sillyCodePieces.GrammarTransformation
import core.transformation.{Contract, MetaObject, TransformationState}
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.simpleBytecode.{InferredMaxStack, InferredStackFrames}
import transformations.javac.classes.ClassCompiler
import transformations.javac.statements.{BlockC, StatementC}
import transformations.types.{ObjectTypeC, TypeC, VoidTypeC}

object MethodC extends GrammarTransformation {

  override def dependencies: Set[Contract] = Set(BlockC, InferredMaxStack, InferredStackFrames)

  def convertMethod(method: MetaObject, classCompiler: ClassCompiler, state: TransformationState) {
    //TODO don't depend on classCompiler and don't get called directly from ClassC.

    def getMethodDescriptorIndex(method: MetaObject): Int = classCompiler.constantPool.store(getMethodDescriptor(method))

    addMethodFlags(method)
    val methodNameIndex: Int = classCompiler.getMethodNameIndex(getMethodName(method))
    method(ByteCodeSkeleton.MethodNameIndex) = methodNameIndex
    method.data.remove(MethodNameKey)
    val methodDescriptorIndex = getMethodDescriptorIndex(method)
    method(ByteCodeSkeleton.MethodDescriptorIndex) = methodDescriptorIndex
    addCodeAnnotation(method)
    method.data.remove(ReturnTypeKey)
    method.data.remove(MethodParametersKey)

    def addCodeAnnotation(method: MetaObject) {
      val parameters = getMethodParameters(method)
      setMethodCompiler(method, parameters)
      val statements = getMethodBody(method)
      val statementToInstructions = StatementC.getToInstructions(state)
      val instructions = statements.flatMap(statement => statementToInstructions(statement))
      val codeIndex = classCompiler.constantPool.store(ByteCodeSkeleton.CodeAttributeId)
      val exceptionTable = Seq[MetaObject]()
      val codeAttributes = Seq[MetaObject]()
      val codeAttribute = new MetaObject(ByteCodeSkeleton.CodeKey) {
        data.put(AttributeNameKey, codeIndex)
        data.put(CodeMaxLocalsKey, getMethodCompiler(state).variables.localCount)
        data.put(CodeInstructionsKey, instructions)
        data.put(CodeExceptionTableKey, exceptionTable)
        data.put(CodeAttributesKey, codeAttributes)
      }
      method(ByteCodeSkeleton.MethodAnnotations) = Seq(codeAttribute)
    }

    def setMethodCompiler(method: MetaObject, parameters: Seq[MetaObject]) {
      val methodCompiler = new MethodCompiler(state)
      if (!getMethodStatic(method))
        methodCompiler.variables.add("this", ObjectTypeC.objectType(classCompiler.currentClassInfo.name))
      for (parameter <- parameters)
        methodCompiler.variables.add(getParameterName(parameter), getParameterType(parameter))
      getState(state).methodCompiler = methodCompiler
    }
  }

  def getMethodDescriptor(method: MetaObject): MetaObject = {
    val returnType = getMethodReturnType(method)
    val parameters = getMethodParameters(method)
    ByteCodeSkeleton.methodDescriptor(returnType, parameters.map(p => getParameterType(p)))
  }

  def getMethodReturnType(metaObject: MetaObject) = {
    metaObject(ReturnTypeKey).asInstanceOf[MetaObject]
  }

  def getMethodParameters(metaObject: MetaObject) = {
    metaObject(MethodParametersKey).asInstanceOf[Seq[MetaObject]]
  }

  def getParameterType(metaObject: MetaObject) = metaObject(ParameterTypeKey).asInstanceOf[MetaObject]

  def addMethodFlags(method: MetaObject) = {
    var flags = Set[ByteCodeSkeleton.MethodAccessFlag]()
    if (getMethodStatic(method))
      flags += ByteCodeSkeleton.StaticAccess

    getMethodVisibility(method) match {
      case MethodC.PublicVisibility => flags += ByteCodeSkeleton.PublicAccess
      case MethodC.PrivateVisibility => flags += ByteCodeSkeleton.PrivateAccess
    }

    method(ByteCodeSkeleton.MethodAccessFlags) = flags
  }

  def getMethodVisibility(method: MetaObject) = method(VisibilityKey).asInstanceOf[Visibility]

  def getMethodStatic(method: MetaObject) = method(StaticKey).asInstanceOf[Boolean]

  def getMethodCompiler(state: TransformationState) = getState(state).methodCompiler

  private def getState(state: TransformationState): State = {
    state.data.getOrElseUpdate(this, new State()).asInstanceOf[State]
  }

  def getMethodBody(metaObject: MetaObject) = metaObject(MethodBodyKey).asInstanceOf[Seq[MetaObject]]

  def getMethodName(method: MetaObject) = {
    method(MethodNameKey).asInstanceOf[String]
  }

  def getParameterName(metaObject: MetaObject) = metaObject(ParameterNameKey).asInstanceOf[String]

  override def transformGrammars(grammars: GrammarCatalogue) {
    val block = grammars.find(BlockC.BlockGrammar)

    val parseType = grammars.find(TypeC.TypeGrammar)
    val parseReturnType = "void" ^^ (_ => VoidTypeC.voidType) | parseType

    val parseParameter = parseType ~ identifier ^^ {
      case _type seqr _name => parameter(_name.asInstanceOf[String], _type)
    }
    val parseParameters = "(" ~> parseParameter.someSeparated(",") <~ ")"
    val parseStatic = "static" ^^ (_ => true) | produce(false)
    val visibilityModifier =
      "public" ^^ (_ => PublicVisibility) |
        "protected" ^^ (_ => ProtectedVisibility) |
        "private" ^^ (_ => PrivateVisibility) |
        produce(DefaultVisibility)

    grammars.create(MethodGrammar, visibilityModifier ~ parseStatic ~ parseReturnType ~ identifier ~
      parseParameters ~ block ^^ {
      case visibility seqr static seqr returnType seqr name seqr parameters seqr body =>
        method(name.asInstanceOf[String], returnType, parameters.asInstanceOf[Seq[MetaObject]], body.asInstanceOf[Seq[MetaObject]],
          static.asInstanceOf[Boolean], visibility.asInstanceOf[Visibility])
    })
  }

  def method(name: String, _returnType: Any, _parameters: Seq[MetaObject], _body: Seq[MetaObject],
             static: Boolean = false, visibility: Visibility = PrivateVisibility) = {
    new MetaObject(ByteCodeSkeleton.MethodInfoKey) {
      data.put(MethodNameKey, name)
      data.put(ReturnTypeKey, _returnType)
      data.put(MethodParametersKey, _parameters)
      data.put(MethodBodyKey, _body)
      data.put(StaticKey, static)
      data.put(VisibilityKey, visibility)
    }
  }

  def parameter(name: String, _type: Any) = {
    new MetaObject("JavaParameter") {
      data.put(ParameterNameKey, name)
      data.put(ParameterTypeKey, _type)
    }
  }

  class State() {
    var methodCompiler: MethodCompiler = null
  }

  class Visibility


  object MethodGrammar

  object MethodBodyKey

  object ParameterNameKey

  object StaticKey

  object VisibilityKey

  object ReturnTypeKey

  object MethodNameKey

  object MethodParametersKey


  object ParameterTypeKey

  object PublicVisibility extends Visibility

  object ProtectedVisibility extends Visibility

  object PrivateVisibility extends Visibility

  object DefaultVisibility extends Visibility

}