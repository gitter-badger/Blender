package transformations.bytecode.attributes

import core.particles.ParticleWithGrammar
import core.particles.grammars.GrammarCatalogue
import core.particles.node.Node
import transformations.bytecode.ByteCodeSkeleton

object UnParsedAttribute extends ParticleWithGrammar {

  class UnParsedAttribute(val node: Node) {
    def nameIndex: Int = node(UnParsedAttribute.UnParsedAttributeName).asInstanceOf[Int]
    def nameIndex_=(value: Int) = node(UnParsedAttribute.UnParsedAttributeName) = value

    def data = node(UnParsedAttribute.UnParsedAttributeData).asInstanceOf[Seq[Byte]]
    def data_=(value: Seq[Byte]) = node(UnParsedAttribute.UnParsedAttributeData) = value
  }

  def construct(nameIndex: Int, bytes: Seq[Byte]) = new Node(UnParsedAttributeKey, UnParsedAttributeName -> nameIndex, UnParsedAttributeData -> bytes)

  object UnParsedAttributeKey
  object UnParsedAttributeName
  object UnParsedAttributeData

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val grammar = "UnParsed attribute with nameIndex:" ~~> integer ^^ parseMap(UnParsedAttributeKey, UnParsedAttributeName)
    grammars.find(ByteCodeSkeleton.AttributeGrammar).addOption(grammar)
  }

  override def description: String = "An attribute whose data has not been parsed yet"
}
