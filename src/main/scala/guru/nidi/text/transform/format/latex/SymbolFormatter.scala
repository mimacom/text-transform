package guru.nidi.text.transform.format.latex

import guru.nidi.text.transform.{TransformContext, Segment}
import guru.nidi.text.transform.Attribute._
import guru.nidi.text.transform.AttributeValue._
import scala.Some

/**
 * Formats an image element.
 */
object SymbolFormatter {

  val symbols = Map(
    ARROW_LEFT -> "$\\leftarrow$ ",
    ARROW_RIGHT -> "$\\rightarrow$ ",
    ARROW_BOTH -> "$\\leftrightarrow$ ",
    DOUBLE_ARROW_LEFT -> "$\\Leftarrow$ ",
    DOUBLE_ARROW_RIGHT -> "$\\Rightarrow$ ",
    DOUBLE_ARROW_BOTH -> "$\\Leftrightarrow$ "
  )

  def format(context: TransformContext, segment: Segment) =
    symbols.get(segment(TYPE).get) match {
      case None => segment(ORIGINAL).get
      case Some(s) => s
    }
}
