package app.holybook.lib.models

import app.holybook.lib.parsers.buildDocument
import app.holybook.lib.parsers.getAttributeOrNull
import app.holybook.lib.parsers.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.w3c.dom.Document

data class BookContent(
  val id: String,
  val language: String,
  val title: String,
  val author: String,
  val publishedAt: LocalDate?,
  val paragraphs: List<Paragraph>
)

fun Document.toBookContent(): BookContent {
  val bookElement = this.documentElement
  val paragraphs =
    bookElement.childNodes
      .map { node ->
        ParagraphElement(
          node.textContent,
          node.getAttributeOrNull("type")?.let { ParagraphType.fromValue(it) } ?: ParagraphType.BODY
        )
      }
      .withIndices()
  return BookContent(
    id = bookElement.getAttribute("id"),
    language = bookElement.getAttribute("language"),
    title = bookElement.getAttribute("title"),
    author = bookElement.getAttribute("author"),
    publishedAt =
      bookElement.getAttributeOrNull("publishedAt")?.let {
        LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
      },
    paragraphs = paragraphs
  )
}

fun BookContent.toXmlDocument() = buildDocument { doc ->
  val bookElement = doc.createElement("book")
  bookElement.setAttribute("id", id)
  bookElement.setAttribute("title", title)
  bookElement.setAttribute("author", author)
  if (publishedAt != null) {
    bookElement.setAttribute("publishedAt", publishedAt.format(DateTimeFormatter.ISO_DATE))
  }
  paragraphs.forEach { p ->
    val paragraphElement = doc.createElement("p")
    paragraphElement.setAttribute("type", p.type.value)
    paragraphElement.textContent = p.text
    bookElement.appendChild(paragraphElement)
  }
  doc.appendChild(bookElement)
}
