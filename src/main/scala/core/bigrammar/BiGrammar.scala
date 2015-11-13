package core.bigrammar

import core.document.{BlankLine, WhiteSpace}
import core.grammar.{Grammar, PrintGrammar, ~}
import core.particles.node.Node
import core.responsiveDocument.ResponsiveDocument



trait BiGrammar extends GrammarDocumentWriter {

  override def toString = PrintGrammar.toDocument(BiGrammarToGrammar.toGrammar(this)).renderString(trim = false)

  lazy val height = 1

  def <~(right: BiGrammar) = new Sequence(this, right).ignoreRight

  def <~~(right: BiGrammar) = this <~ (space ~ right)

  def manySeparated(separator: BiGrammar): BiGrammar = someSeparated(separator) | new Produce(Seq.empty[Any])

  def |(other: BiGrammar) = new Choice(this, other)

  def ~~(right: BiGrammar): BiGrammar = {
    (this <~ space) ~ right
  }

  def someSeparatedVertical(separator: BiGrammar): BiGrammar =
    this % new ManyVertical(separator %> this) ^^ someMap

  def manyVertical = new ManyVertical(this)

  def manySeparatedVertical(separator: BiGrammar): BiGrammar = someSeparatedVertical(separator) | new Produce(Seq.empty[Node])

  def option: BiGrammar = this ^^ (x => Some(x), x => x.asInstanceOf[Option[Any]]) | produce(None)
  def some: BiGrammar = this ~ (this*) ^^ someMap
  def someSeparated(separator: BiGrammar): BiGrammar = this ~ ((separator ~> this) *) ^^ someMap
  def children: Seq[BiGrammar] = Seq.empty

  private def someMap: ((Any) => Seq[Any], (Any) => Option[~[Any, Seq[Any]]]) = {
    ( {
      case first ~ rest => Seq(first) ++ rest.asInstanceOf[Seq[Any]]
    }, {
      case seq: Seq[Any] => if (seq.nonEmpty) Some(core.grammar.~(seq.head, seq.tail)) else None
    })
  }

  def optionToSeq: BiGrammar = new MapGrammar(this,
    option => option.asInstanceOf[Option[Any]].fold(Seq.empty[Any])(x => Seq(x)), {
      case seq:Seq[Any] => Some(if (seq.isEmpty) None else Some(seq))
      case _ => None
    })
  def seqToSet: BiGrammar = new MapGrammar(this, seq => seq.asInstanceOf[Seq[Any]].toSet, set => Some(set.asInstanceOf[Set[Any]].toSeq))

  def inParenthesis = ("(": BiGrammar) ~> this <~ ")"

  def ~(other: BiGrammar) = new Sequence(this, other)

  def ~>(right: BiGrammar): BiGrammar = new Sequence(this, right).ignoreLeft

  def ~~>(right: BiGrammar) = (this ~ space) ~> right

  def * = new ManyHorizontal(this)
  def many = new ManyHorizontal(this)

  def %(bottom: BiGrammar) = new TopBottom(this, bottom)

  def %%(bottom: BiGrammar): BiGrammar = {
    (this %< BlankLine) % bottom
  }

  def %>(bottom: BiGrammar) = new TopBottom(this, bottom).ignoreLeft

  def %<(bottom: BiGrammar) = new TopBottom(this, bottom).ignoreRight

  def ^^(map: (Any => Any, Any => Option[Any])): BiGrammar = new MapGrammar(this, map._1, map._2)

  def indent(width: Int = 2) = new WhiteSpace(width, 0) ~> this

  def deepClone: BiGrammar = new DeepCloneBiGrammar().observe(this)
}

class DeepCloneBiGrammar extends BiGrammarObserver[BiGrammar] {

  override def labelledEnter(name: AnyRef): BiGrammar = new Labelled(name)

  override def labelledLeave(inner: BiGrammar, partial: BiGrammar): Unit = partial.asInstanceOf[Labelled].inner = inner

  override def handleGrammar(self: BiGrammar, helper: (BiGrammar) => BiGrammar): BiGrammar = self match {
    case choice:Choice => new Choice(helper(choice.left), helper(choice.right))
    case many: ManyVertical => new ManyVertical(helper(many.inner))
    case many: ManyHorizontal => new ManyHorizontal(helper(many.inner))
    case mapGrammar: MapGrammar => new MapGrammar(helper(mapGrammar.inner), mapGrammar.construct, mapGrammar.deconstruct)
    case sequence:Sequence => new Sequence(helper(sequence.first), helper(sequence.second))
    case topBottom: TopBottom => new TopBottom(helper(topBottom.first), helper(topBottom.second))
    case _ => self
  }
}

trait BiGrammarObserver[Result] {

  def labelledEnter(name: AnyRef): Result

  def labelledLeave(inner: Result, partial: Result)

  def handleGrammar(self: BiGrammar, recursive: BiGrammar => Result): Result

  def observe(grammar: BiGrammar) = {
    var cache = Map.empty[Labelled, Result]
    def helper(grammar: BiGrammar): Result = grammar match {
      case labelled: Labelled =>
        cache.getOrElse(labelled, {
          val result = labelledEnter(labelled.name)
          cache += labelled -> result
          labelledLeave(helper(labelled.inner), result)
          result
        })
      case _ => handleGrammar(grammar, helper)
    }
    helper(grammar)
  }
}

trait SequenceLike extends BiGrammar {
  def first: BiGrammar
  def second: BiGrammar
  def ignoreLeft: MapGrammar = {
    new MapGrammar(this, { case ~(l, r) => r}, r => Some(core.grammar.~(MissingValue, r)))
  }

  def ignoreRight: MapGrammar = {
    new MapGrammar(this, { case ~(l, r) => l}, l => Some(core.grammar.~(l, MissingValue)))
  }
}

case class Delimiter(value: String) extends BiGrammar

case class Keyword(value: String, reserved: Boolean = true) extends BiGrammar

case class Consume(grammar: Grammar) extends BiGrammar

abstract class Many(var inner: BiGrammar) extends BiGrammar
{
  override def children = Seq(inner)
}

class ManyVertical(inner: BiGrammar) extends Many(inner)

class ManyHorizontal(inner: BiGrammar) extends Many(inner)

object MissingValue
{
  override def toString = "_"
}

class Choice(var left: BiGrammar, var right: BiGrammar) extends BiGrammar
{
  override def children = Seq(left, right)
}

class Sequence(var first: BiGrammar, var second: BiGrammar) extends BiGrammar with SequenceLike
{
  override def children = Seq(first, second)
}

class MapGrammar(var inner: BiGrammar, val construct: Any => Any, val deconstruct: Any => Option[Any]) extends BiGrammar
{
  override def children = Seq(inner)
}

class Labelled(val name: AnyRef, var inner: BiGrammar = BiFailure) extends BiGrammar {

  def addOption(addition: BiGrammar) {
    inner = inner | addition
  }

  override def children = Seq(inner)
}

class TopBottom(var first: BiGrammar, var second: BiGrammar) extends BiGrammar with SequenceLike {
  override lazy val height = first.height + second.height

  override def children = Seq(first, second)
}

case class Print(document: ResponsiveDocument) extends BiGrammar

case class Produce(result: Any) extends BiGrammar

object BiFailure extends BiGrammar