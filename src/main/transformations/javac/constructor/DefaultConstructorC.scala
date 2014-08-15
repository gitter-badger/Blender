package transformations.javac.constructor

import core.transformation.sillyCodePieces.ProgramTransformation
import core.transformation.{Contract, MetaObject, TransformationState}
import transformations.javac.classes.ClassC
import transformations.javac.methods.MethodC.PublicVisibility

object DefaultConstructorC extends ProgramTransformation {
  override def dependencies: Set[Contract] = Set(ConstructorC)

  def transform(program: MetaObject, state: TransformationState): Unit = {
    transformClass(program)

    def transformClass(clazz: MetaObject) {
      ClassC.getMethods(clazz).prepend(ConstructorC.constructor(Seq(), Seq(), PublicVisibility))
    }
  }
}