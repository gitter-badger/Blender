package transformations.bytecode.attributes

import core.bigrammar.BiGrammar
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Key, Node}
import core.particles.{CompilationState, Contract}
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.readJar.ClassFileParser

case class LineNumberRef(lineNumber: Int, startProgramCounter: Int)

object LineNumberTable extends ByteCodeAttribute {
  override def dependencies: Set[Contract] = Set(ByteCodeSkeleton)

  def lineNumberTable(nameIndex: Int, lines: Seq[LineNumberRef]) = new Node(LineNumberTableKey,
    ByteCodeSkeleton.AttributeNameKey -> nameIndex,
    LineNumberTableLines -> lines)

  override def inject(state: CompilationState): Unit = {
    super.inject(state)
    ByteCodeSkeleton.getState(state).getBytes(LineNumberTableKey) = getLineNumberTableBytes
  }

  def getLineNumberTableBytes(attribute: Node): Seq[Byte] = {
    val entries = LineNumberTable.getLineNumberTableEntries(attribute)
    shortToBytes(entries.length) ++
      entries.flatMap(getLineNumberTableEntryByteCode)
  }

  def getLineNumberTableEntryByteCode(entry: LineNumberRef) =
    shortToBytes(entry.startProgramCounter) ++ shortToBytes(entry.lineNumber)

  def getLineNumberTableEntries(lineNumberTable: Node) = lineNumberTable(LineNumberTableLines).asInstanceOf[Seq[LineNumberRef]]

  object LineNumberTableKey extends Key

  object LineNumberTableLines

  override def description: String = "Defines the line number table attribute. " +
    "This table explains which source code line a particular instruction came from, and can be used to aid in debugging."

  override def key: Key = LineNumberTableKey

  override def getGrammar(grammars: GrammarCatalogue): BiGrammar = "Not implemented" ^^ parseMap(key) // TODO implement. Also figure out why I can't use failure here.

  override def constantPoolKey: String = "LineNumberTable"

  override def getParser(unParsed: Node): ClassFileParser.Parser[Node] = ???
}
