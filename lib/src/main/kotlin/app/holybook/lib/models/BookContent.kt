package app.holybook.lib.models

import app.holybook.lib.parsers.buildDocument
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BookContent(
  val title: String,
  val author: String,
  val publishedAt: LocalDate?,
  val paragraphs: List<Paragraph>
)

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
