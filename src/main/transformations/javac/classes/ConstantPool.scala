package transformations.javac.classes

import core.particles.node.Node
import transformations.bytecode.constants.ClassRefConstant

import scala.collection.mutable

class ConstantPool(items: Seq[Any] = Seq.empty) {
  val constants: mutable.Buffer[Any] = new mutable.ArrayBuffer[Any]() ++ items
  val reverseRouter = mutable.Map[Any, Int]()
  for (indexedConstant <- constants.zipWithIndex)
    reverseRouter(indexedConstant._1) = indexedConstant._2 + 1

  def getNode(index: Int) = getValue(index).asInstanceOf[Node]
  def getUtf8(index: Int) = getValue(index).asInstanceOf[String]
  def getValue(index: Int) = constants(index - 1)

  def getClassRef(nameParts: QualifiedClassName): Int = {
    val nameIndex = store(nameParts)
    store(ClassRefConstant.classRef(nameIndex))
  }

  def store(ref: Any): Int = {
    reverseRouter.getOrElse[Int](ref, {
      val index = constants.length + 1
      reverseRouter(ref) = index
      constants.append(ref)
      index
    })
  }

  def storeUtf8(value: String) = {
    store(value)
  }
}
