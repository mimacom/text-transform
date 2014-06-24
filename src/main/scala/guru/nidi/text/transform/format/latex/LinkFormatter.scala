package guru.nidi.text.transform.format.latex

import guru.nidi.text.transform.{TransformContext, Segment}
import guru.nidi.text.transform.Attribute._
import guru.nidi.text.transform.AttributeValue._

/**
 *
 */
object LinkFormatter {
  def format(context: TransformContext, segment: Segment) = {
    val target = segment(TARGET).get

    def link = {
      val ch = LatexFormatter.formatCaption(context, segment)
      val (text, rawUrl) = context.processLink(segment, ch, target)
      val url = escape(rawUrl)
      s"\\href{$url}{$text}"
    }

    def escape(url: String) = url.replace("%", "\\%")

    def document = {
      segment(SUB) match {
        case Some(seg: Segment) => LatexFormatter.formatChildren(context, seg)
        case _ => ""
      }
    }

    segment(TYPE) match {
      case Some(URL) => link
      case Some(DOCUMENT_REF) => document
      case Some(DOCUMENT_INCLUDE) => LatexFormatter.formatChildren(context, segment)
      case Some(FILE_REF) => LatexFormatter.formatChildren(context, segment) + s"\\label{$target}"
      case _ => s"\\autoref{$target}"
    }
  }
}
