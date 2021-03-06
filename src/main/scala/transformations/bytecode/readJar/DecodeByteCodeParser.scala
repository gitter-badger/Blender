package transformations.bytecode.readJar

import java.io.{BufferedInputStream, InputStream}

import core.grammar.ParseException
import core.particles.node.Node
import core.particles.{CompilationState, Particle}

object DecodeByteCodeParser extends Particle {
  override def inject(state: CompilationState): Unit = {
    state.parse = decodeStream
  }

  def decodeStream(inputStream: InputStream): Node = {
    val bis = new BufferedInputStream(inputStream)
    val inputBytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte)
    val parseResult = ClassFileParser.packratParser(new ArrayReader(0, inputBytes))
    if (!parseResult.successful)
      throw new ParseException(parseResult.toString)

    val clazz = parseResult.get
    clazz
  }

  override def description: String = "Decodes a binary bytecode classfile."
}
