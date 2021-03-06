package core.bigrammar.printer

import core.bigrammar.BiGrammar
import core.responsiveDocument.ResponsiveDocument

trait PrintError extends Throwable
 {
   override def toString = toDocument.renderString()
   def toDocument: ResponsiveDocument
   def partial: ResponsiveDocument
   val depth: Int
   def mapPartial(f: ResponsiveDocument => ResponsiveDocument): PrintError
 }
