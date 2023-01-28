package app.holybook.import

import app.holybook.lib.models.Paragraph
import java.time.LocalDate

class OriginalBook(
    val metadata: BookMetadata,
    val content: BookContent
)

class BookContent(
    val title: String,
    val author: String,
    val paragraphs: List<Paragraph>
)

class BookMetadata(
    val publishedAt: LocalDate?
)