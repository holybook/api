package app.holybook.import.content.parsers

import app.holybook.lib.models.BookContent
import app.holybook.lib.models.Paragraph
import java.time.LocalDate

data class ParsedBook(
  val title: String,
  val author: String,
  val publishedAt: LocalDate?,
  val paragraphs: List<Paragraph>
)

fun ParsedBook.withIdAndLanguage(id: String, language: String) =
  BookContent(id, language, title, author, publishedAt, paragraphs)
