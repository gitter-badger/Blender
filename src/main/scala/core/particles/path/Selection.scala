package core.particles.path

import core.particles.node.Node

case class Selection(parent: Path, field: Any) extends OriginWithParent {
  val current = parent.current(field).asInstanceOf[Node]
  override def parentOption: Option[Path] = Some(parent)

  override def hashCode(): Int = parent.hashCode() * field.hashCode()

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: Selection => other.parent.equals(parent) && other.field.equals(field)
    case _ => false
  }

  override def replaceWith(replacement: Node): Unit = parent(field) = replacement //TODO hier hoort nog .obj. Hoezo compiled dit?

  override def pathAsString: String = s"${parent.pathAsString}.$field"
}
