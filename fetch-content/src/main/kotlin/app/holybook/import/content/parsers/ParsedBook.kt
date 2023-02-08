package app.holybook.import.content.parsers

import app.holybook.lib.models.BookContent
import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.models.Paragraph

data class ParsedBook(val title: String, val paragraphs: List<Paragraph>)

fun ParsedBook.withContentDescriptor(descriptor: ContentDescriptor) =
  BookContent(
    descriptor.id,
    descriptor.language,
    title,
    descriptor.authorCode,
    descriptor.publishedAt,
    paragraphs
  )
