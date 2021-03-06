package transformations.javac.classes.skeleton

import core.particles.CompilationState
import transformations.javac.JavaLang
import transformations.javac.classes.{MethodQuery, MethodInfo}

case class MyCompiler(state: CompilationState) {
  val classPath: PackageSignature = JavaLang.classPath

  def getPackage(parts: List[String]): PackageSignature = classPath.getPackage(parts)

  def find(methodId: MethodQuery): MethodInfo = find(methodId.className.parts)
    .asInstanceOf[ClassSignature].getMethod(methodId)

  def find(parts: Seq[String]): PackageMember = parts.foldLeft[PackageMember](classPath)(
    (pck: PackageMember, part: String) => pck.asInstanceOf[PackageSignature].content(part))
}