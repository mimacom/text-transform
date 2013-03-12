package org.mimacom.text.transform.parse.html

import org.mimacom.text.transform.{AttributeValue, Segment, Parser}
import org.mimacom.text.transform.Name._
import org.mimacom.text.transform.Attribute._
import xml.{Text, XML, Node}
import collection.mutable.ListBuffer
import java.util.regex.Pattern

/**
 *
 */
class HtmlParser extends Parser {
  val LINK_PATTERN=Pattern.compile("(https?://[^\\Q (,.?!:;\"')\\E]*)")

  def parse(input: String): Segment = {
    val result = Segment(ROOT)
    val xml = XML.loadString("<root>" + input + "</root>")
    parse(xml,1)(0)
  }

  def parse(xml: Seq[Node],listLevel:Int): Seq[Segment] = {
    val res = new ListBuffer[Segment]
    for (node <- xml) {
      res ++= parse(node,listLevel)
    }
    res
  }

  def parse(node: Node,listLevel:Int): Seq[Segment] = {
    node match {
      case <root>{ns @ _*}</root> => List(Segment(ROOT, parse(ns,listLevel): _*))
      case <strong>{ns @ _*}</strong> if (!ns.isEmpty) => List(Segment(BOLD, parse(ns,listLevel): _*))
      case <p>{ns @ _*}</p> if (!ns.isEmpty) => parse(ns,listLevel) ++ List(Segment(PLAIN,TEXT->"\r\n\r\n"))
      case <span>{ns @ _*}</span> if (!ns.isEmpty) => parse(ns,listLevel)
      case <br/> => List(Segment(PLAIN,TEXT->"\r\n\r\n"))
      case <hr/> => List(Segment(LINE))
      case <em>{ns @ _*}</em> if (!ns.isEmpty) => List(Segment(ITALICS, parse(ns,listLevel): _*))
      case <h1>{ns @ _*}</h1> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->1))
      case <h2>{ns @ _*}</h2> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->2))
      case <h3>{ns @ _*}</h3> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->3))
      case <h4>{ns @ _*}</h4> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->4))
      case <h5>{ns @ _*}</h5> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->5))
      case <h6>{ns @ _*}</h6> if (!ns.isEmpty) => List(Segment(HEADING, parse(ns,listLevel): _*).add(LEVEL->6))
      case <ol>{ns @ _*}</ol> if (!ns.isEmpty) => List(Segment(LIST, parse(ns,listLevel+1): _*).add(TYPE->AttributeValue.ORDERED,LEVEL->listLevel))
      case <ul>{ns @ _*}</ul> if (!ns.isEmpty) => List(Segment(LIST, parse(ns,listLevel+1): _*).add(TYPE->AttributeValue.UNORDERED,LEVEL->listLevel))
      case <li>{ns @ _*}</li> if (!ns.isEmpty) => List(Segment(LIST_ITEM, parse(ns,listLevel): _*))
      case n @ <a>{ns @ _*}</a> =>
        val href = n \ "@href" text
        val desc = if (ns.isEmpty) List(Segment.plainText(href)) else parse(ns,listLevel)
        List(Segment(LINK, desc: _*).add(TARGET -> href))
      case Text(t) =>
        val list = new ListBuffer[Segment]
        val s = new StringBuffer
        val m = LINK_PATTERN.matcher(t)
        while (m.find) {
          m.appendReplacement(s, "")
          list += Segment.plainText(s.toString)
          list += Segment(LINK, Segment.plainText(m.group(1)), TARGET -> m.group(1))
          s.setLength(0)
        }
        m.appendTail(s)
        list += Segment.plainText(s.toString)
        list
      case _ => Nil
    }
  }

}