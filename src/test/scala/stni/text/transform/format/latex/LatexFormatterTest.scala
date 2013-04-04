package stni.text.transform.format.latex

import stni.text.transform.format.FormatterTest
import stni.text.transform._
import stni.text.transform.Segment._
import stni.text.transform.Attribute._
import stni.text.transform.AttributeValue._
import stni.text.transform.Name._
import java.util.Locale
import scala.Some


/**
 *
 */
class LatexFormatterTest extends FormatterTest {
  val resourceLoader = new ResourceLoader {
    def loadResource(source: Segment, name: String) = if (name == "nix") None else Some("load:" + name)
  }

  val formatter = new LatexFormatter(new TransformContext(2, Locale.GERMAN, resourceLoader))

  behavior of "basic formatting"

  it should "simply replace formats" in {
    "hallo" formatOf plain("hallo")
    "\\\\ \\hline \\noindent \\\\" formatOf LINE()
    "\\\\" formatOf NEWLINE()
    "\\textbf{hallo}" formatOf BOLD(plain("hallo"))
    "\\textit{hallo}" formatOf ITALICS(plain("hallo"))
  }

  it should "support mixing with plain text" in {
    "a\\textbf{hallo}b" formatOf ROOT(
      plain("a"),
      BOLD(plain("hallo")),
      plain("b"))
  }

  it should "support nesting" in {
    "a\\textbf{c\\textit{d}e}b" formatOf ROOT(
      plain("a"),
      BOLD(plain("c"), ITALICS(plain("d")), plain("e")),
      plain("b"))
  }

  behavior of "definition"

  it should "translate into description environment" in {
    "\\begin{description}[leftmargin=5cm,style=sameline]\\item[a]blu\\\\\\textbf{blu2}\\end{description}" formatOf DEFINITION(
      TEXT -> "a",
      ITEM(plain("blu")),
      ITEM(BOLD(plain("blu2"))))
  }

  it should "translate WIDTH property into leftmargin option" in {
    "\\begin{description}[leftmargin=7cm,style=sameline]\\item[a]blu\\end{description}" formatOf DEFINITION(
      WIDTH -> "7cm",
      TEXT -> "a",
      ITEM(plain("blu")))
  }

  behavior of "list"

  it should "translate unordered list into an itemize environment" in {
    "\\begin{itemize} \\item{a}\\item{b}\\end{itemize}" formatOf LIST(
      TYPE -> UNORDERED,
      ITEM(plain("a")),
      ITEM(plain("b")))
  }

  it should "translate unordered list into an enumerate environment" in {
    "\\begin{enumerate} \\item{a}\\item{b}\\end{enumerate}" formatOf LIST(
      TYPE -> ORDERED,
      ITEM(plain("a")),
      ITEM(plain("b")))
  }

  behavior of "link"

  it should "translate external link into href and support nested format" in {
    "a\\href{http://}{d1\\textbf{d2}}b" formatOf ROOT(
      plain("a"),
      LINK(TARGET -> "http://", TYPE -> URL, plain("d1"), BOLD(plain("d2"))),
      plain("b"))
  }

  it should "translate link to image into ref with 'Abbildung'" in {
    "aAbbildung \\ref{image:img}b" formatOf ROOT(
      plain("a"),
      LINK(TARGET -> "img", TYPE -> IMAGE_REF, plain("d1"), BOLD(plain("d2"))),
      plain("b"))
  }

  it should "translate other links into plain ref" in {
    "a\\ref{other}b" formatOf ROOT(
      plain("a"),
      LINK(TARGET -> "other", plain("d1"), BOLD(plain("d2"))),
      plain("b"))
  }

  it should "support reference links to other documents" in {
    "starta\\textbf{b}eee" formatOf ROOT(
      plain("start"),
      LINK(TYPE -> DOCUMENT_REF, TARGET -> "sub", SUB -> ROOT(plain("a"), BOLD(plain("b")))),
      plain("eee"))
  }

  it should "support include links to other documents" in {
    "starta\\textbf{b}eee" formatOf ROOT(
      plain("start"),
      LINK(TYPE -> DOCUMENT_INCLUDE, TARGET -> "sub", plain("a"), BOLD(plain("b"))),
      plain("eee"))
  }

  behavior of "heading"

  it should "translate into the correct element of section/paragraph and not support nested format but correct escaping" in {
    "\\section{**hallo**}" formatOf HEADING(plain("**hallo**"))
    "\\subsection{**hallo**}" formatOf HEADING(LEVEL -> 1, plain("**hallo**"))
    "\\subparagraph{**hallo**}" formatOf HEADING(LEVEL -> 10000, plain("**hallo**"))
    "\\subsubsection{Widget: \"`achtung\"'}" formatOf HEADING(LEVEL -> 2, plain("Widget: \"achtung\""))
  }

  behavior of "special characters"

  it should "escape correctly" in {
    "\n \\textbackslash  \\# \\$ \\% \\& \\_ \\{ \\} {[} {]} \\~{} \\^{} \"`xxx\"' „xxx\"'" formatOf
      plain("\n \\ # $ % & _ { } [ ] ~ ^ \"xxx\" „xxx\"")
  }

  behavior of "symbols"

  it should "translate into corresponding latex symbols" in {
    def symbol(typ: AttributeValue) = Segment.symbol("", typ)

    "$\\leftarrow$ $\\rightarrow$ $\\Leftarrow$ $\\Rightarrow$ $\\leftrightarrow$ $\\Leftrightarrow$ " formatOf ROOT(
      symbol(ARROW_LEFT), symbol(ARROW_RIGHT),
      symbol(DOUBLE_ARROW_LEFT), symbol(DOUBLE_ARROW_RIGHT),
      symbol(ARROW_BOTH), symbol(DOUBLE_ARROW_BOTH))
  }

  behavior of "image"

  val start = "\\begin{figure}[hpt] \\centering\n\\includegraphics["
  val end = "]{load:target}\n\\caption{bild} \\label{image:bild}\n\\end{figure}"
  val image = IMAGE(TARGET -> "target", FLOAT -> true, plain("bild"))

  it should "translate into figure environment if floating and have textwidth if no width is specified" in {
    start + "width=1.0\\textwidth" + end formatOf image
  }

  it should "understand absolute width attribute" in {
    start + "width=5cm" + end formatOf image(WIDTH -> "5cm")
  }

  it should "understand relative width attribute" in {
    start + "width=0.55\\textwidth" + end formatOf image(WIDTH -> "55%")
  }

  it should "understand angle attribute and support multiple attributes" in {
    start + "height=0.1\\textheight,angle=45" + end formatOf
      image(ANGLE -> "45", HEIGHT -> "10%", WIDTH -> null)
  }

  it should "show an error message it cannot be found" in {
    "Bild nix nicht gefunden" formatOf IMAGE(TARGET -> "nix", plain("bild"))
  }

  it should "make use of captionof if not floating" in {
    val start = "\\begin{center} \\begin{minipage}{\\linewidth} \\includegraphics["
    val end = "]{load:target}\n\\captionof{figure}{bild} \\label{image:bild}\n\\end{minipage}\\end{center}"
    val image = IMAGE(TARGET -> "target", FLOAT -> false, plain("bild"))
    start + "width=1.0\\textwidth" + end formatOf image
  }

  behavior of "table"

  val table = TABLE(ROWS -> 1, COLUMNS -> 2, FLOAT -> true, Attribute("1,1") -> TABLE_CELL(plain("a1")))
  val prefix = "\\begin{table}[hpt] \\centering\n\\begin{longtable}"
  val postfix = "\\end{longtable}\\end{table}"

  it should "translate into a tabular environment wrapped into a table environment if floating" in {
    prefix + "{p{0.45 \\textwidth} p{0.45 \\textwidth} } " + "a1&\\tabularnewline \n" + postfix formatOf table
  }

  it should "understand width attribute" in {
    prefix + "{p{5cm} l } " + "a1&\\tabularnewline \n" + postfix formatOf table(WIDTH(1) -> "5cm")
  }

  it should "underline the header" in {
    prefix + "{p{5cm} l } " + "a1&\\tabularnewline \\hline \\endhead\n" + postfix formatOf
      table(Attribute("1,1") -> TABLE_CELL(HEADER -> true, plain("a1")))
  }

  it should "translate align left into raggedright" in {
    prefix + "{p{5cm} l } " + "\\raggedright a1&\\tabularnewline \n" + postfix formatOf
      table(Attribute("1,1") -> TABLE_CELL(ALIGN -> LEFT, plain("a1")))
  }

  it should "translate align right into reggedleft" in {
    prefix + "{p{5cm} l } " + "\\raggedleft a1&\\tabularnewline \n" + postfix formatOf
      table(Attribute("1,1") -> TABLE_CELL(ALIGN -> RIGHT, plain("a1")))
  }

  it should "translate span into multicolumn" in {
    prefix + "{p{5cm} l } " + "\\multicolumn{2}{l}{a1}\\tabularnewline \n" + postfix formatOf
      table(Attribute("1,1") -> TABLE_CELL(SPAN -> 2, plain("a1")))
  }

  it should "translate into a simple tabular environment if not floating" in {
    val table = TABLE(ROWS -> 1, COLUMNS -> 2, FLOAT -> false,
      CAPTION -> plain("new table \"here\""),
      Attribute("1,1") -> TABLE_CELL(plain("a1")))
    val prefix = "\\begin{center} \\begin{longtable}"
    val postfix = "\\end{longtable}\n" +
      "\\captionof{table}{new table \"`here\"'} \\label{table:new table \"`here\"'}" +
      "\\end{center}"

    prefix + "{p{0.45 \\textwidth} p{0.45 \\textwidth} } " + "a1&\\tabularnewline \n" + postfix formatOf table
  }
}