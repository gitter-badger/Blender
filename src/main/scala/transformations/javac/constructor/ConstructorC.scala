package transformations.javac.constructor

import core.particles._
import core.particles.exceptions.BadInputException
import core.particles.grammars.GrammarCatalogue
import core.particles.node.Node
import transformations.bytecode.coreInstructions.InvokeSpecialC
import transformations.bytecode.coreInstructions.objects.LoadAddressC
import transformations.bytecode.types.VoidTypeC
import transformations.javac.classes.skeleton.JavaClassSkeleton
import transformations.javac.classes.skeleton.JavaClassSkeleton._
import transformations.javac.methods.MethodC
import transformations.javac.methods.MethodC._
import transformations.javac.methods.call.CallStaticOrInstanceC
import transformations.javac.statements.BlockC

object ConstructorC extends ParticleWithGrammar with ParticleWithPhase {

  override def dependencies: Set[Contract] = Set(MethodC, CallStaticOrInstanceC, InvokeSpecialC, LoadAddressC, SuperCallExpression)

  case class BadConstructorNameException(clazz: Node, constructor: Node) extends BadInputException

  override def transform(clazz: Node, state: CompilationState): Unit = {
    val className = clazz.name
    for (constructor <- getConstructors(clazz)) {
      val constructorClassName = constructor(ConstructorClassNameKey).asInstanceOf[String]
      if (!constructorClassName.equals(className))
        throw new BadConstructorNameException(clazz, constructor)

      constructor.clazz = MethodC.MethodKey
      constructor(MethodC.MethodNameKey) = SuperCallExpression.constructorName
      constructor(MethodC.ReturnTypeKey) = VoidTypeC.voidType
      constructor(MethodC.TypeParameters) = Seq.empty
      constructor(StaticKey) = false
      constructor.data.remove(ConstructorClassNameKey)
    }
  }

  def getConstructors(clazz: Node): Seq[Node] = {
    clazz.members.filter(member => member.clazz == ConstructorKey)
  }

  def constructor(className: String, _parameters: Seq[Node], _body: Seq[Node],
                  visibility: Visibility = PublicVisibility) = new Node(ConstructorKey,
    MethodParametersKey -> _parameters, MethodBodyKey -> _body, VisibilityKey -> visibility,
    ConstructorClassNameKey -> className)


  object ConstructorKey

  object ConstructorClassNameKey

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val memberGrammar = grammars.find(JavaClassSkeleton.ClassMemberGrammar)
    val visibilityModifier = grammars.find(MethodC.VisibilityGrammar)
    val parseParameters = grammars.find(MethodC.ParametersGrammar)
    val block = grammars.find(BlockC.BlockGrammar)
    val constructorGrammar = visibilityModifier ~~ identifier ~ parseParameters % block ^^
      parseMap(ConstructorKey, VisibilityKey, ConstructorClassNameKey, MethodParametersKey, MethodBodyKey)
    memberGrammar.addOption(constructorGrammar)
  }

  override def description: String = "Introduces constructors."
}
