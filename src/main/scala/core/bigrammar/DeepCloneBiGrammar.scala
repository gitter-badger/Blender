package core.bigrammar

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
