package application.compilerBuilder

import java.awt.datatransfer.{DataFlavor, Transferable}
import java.awt.dnd.DnDConstants
import javax.swing.{JComponent, JList, TransferHandler}
import application.compilerBuilder.ParticleInstance._
import core.particles.Particle

import scala.collection.convert.Wrappers.JListWrapper

class ParticleProviderTransferHandler(val availableList: JList[_]) extends TransferHandler {

  @Override
  override def createTransferable(comp: JComponent): Transferable = {
    new ListItemTransferable(JListWrapper(availableList.getSelectedValuesList).map(x => x.getParticleInstance).toSeq)
  }

  override def getSourceActions(c: JComponent): Int = DnDConstants.ACTION_COPY

}

object ListItemTransferable {
  val LIST_ITEM_DATA_FLAVOR: DataFlavor = new DataFlavor(classOf[Seq[Particle]], "java/ListItem")
}

case class ListItemTransferable(listItems: Seq[ParticleInstance]) extends Transferable {

  @Override
  def getTransferDataFlavors: Array[DataFlavor] = {
    Array(ListItemTransferable.LIST_ITEM_DATA_FLAVOR)
  }

  @Override
  def isDataFlavorSupported(flavor: DataFlavor): Boolean = {
    flavor.equals(ListItemTransferable.LIST_ITEM_DATA_FLAVOR)
  }

  override def getTransferData(flavor: DataFlavor): AnyRef = listItems
}