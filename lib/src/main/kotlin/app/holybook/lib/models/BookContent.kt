package app.holybook.lib.models

import app.holybook.lib.parsers.buildDocument
import app.holybook.lib.parsers.getAttributeOrNull
import app.holybook.lib.parsers.toList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

data class BookContent(
  val id: String,
  val language: String,
  val title: String,
  val author: String,
  val publishedAt: LocalDate?,
  val paragraphs: List<Paragraph>,
)

fun Document.toBookContent(): BookContent {
  val bookElement = this.documentElement
  val paragraphs = buildParagraphs { appendChildElements(bookElement) }
  return BookContent(
    id = bookElement.getAttribute("id"),
    language = bookElement.getAttribute("language"),
    title = bookElement.getAttribute("title"),
    author = bookElement.getAttribute("author"),
    publishedAt =
      bookElement.getAttributeOrNull("publishedAt")?.let {
        LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
      },
    paragraphs = paragraphs,
  )
}

/**
 * Recursively walks the children of [parent], adding a paragraph for each `<p>` and opening a
 * nested section for each `<section>`. Flat books (only top-level `<p>`) are handled by the same
 * code path with an empty section path.
 */
private fun ParagraphListBuilder.appendChildElements(parent: Element) {
  parent.childNodes.toList().filterIsInstance<Element>().forEach { element ->
    when (element.tagName) {
      "section" -> section(element.getAttribute("title")) { appendChildElements(element) }
      "p" ->
        addParagraph(
          element.textContent,
          element.getAttributeOrNull("type")?.let { ParagraphType.fromValue(it) }
            ?: ParagraphType.BODY,
        )
    }
  }
}

fun BookContent.toXmlDocument() = buildDocument { doc ->
  val bookElement = doc.createElement("book")
  bookElement.setAttribute("id", id)
  bookElement.setAttribute("language", language)
  bookElement.setAttribute("title", title)
  bookElement.setAttribute("author", author)
  if (publishedAt != null) {
    bookElement.setAttribute("publishedAt", publishedAt.format(DateTimeFormatter.ISO_DATE))
  }

  // Reconstruct the section nesting from the flat paragraph list. A SECTION_TITLE paragraph opens a
  // `<section>`; every other paragraph is appended as a `<p>` to the currently open section.
  val openSections = ArrayDeque(listOf("" to bookElement as Node))
  fun parentFor(path: String) {
    while (openSections.last().first != path) {
      openSections.removeLast()
    }
  }
  paragraphs.forEach { p ->
    if (p.type == ParagraphType.SECTION_TITLE) {
      parentFor(p.sectionPath.substringBeforeLast('.', ""))
      val sectionElement = doc.createElement("section")
      sectionElement.setAttribute("title", p.text)
      openSections.last().second.appendChild(sectionElement)
      openSections.addLast(p.sectionPath to sectionElement)
    } else {
      parentFor(p.sectionPath)
      val paragraphElement = doc.createElement("p")
      paragraphElement.setAttribute("type", p.type.value)
      paragraphElement.textContent = p.text
      openSections.last().second.appendChild(paragraphElement)
    }
  }
  doc.appendChild(bookElement)
}
