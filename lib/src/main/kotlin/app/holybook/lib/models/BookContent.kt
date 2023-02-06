package app.holybook.lib.models

import app.holybook.lib.parsers.buildDocument
import app.holybook.lib.parsers.getAttribute
import app.holybook.lib.parsers.getAttributeString
import app.holybook.lib.parsers.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.w3c.dom.Document

data class BookContent(
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
          node.getAttributeString("type")?.let { ParagraphType.fromValue(it) } ?: ParagraphType.BODY
        )
      }
      .withIndices()
  return BookContent(
    title = bookElement.getAttribute("title"),
    author = bookElement.getAttribute("author"),
    publishedAt =
      bookElement.getAttributeString("publishedAt")?.let {
        LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
      },
    paragraphs = paragraphs
  )
}

fun BookContent.toXmlDocument() = buildDocument { doc ->
  val bookElement = doc.createElement("book")
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
