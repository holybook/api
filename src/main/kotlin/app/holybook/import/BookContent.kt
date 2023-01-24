package app.holybook.import

import app.holybook.api.models.Paragraph
import java.time.LocalDate

class OriginalBook(
    val metadata: BookMetadata,
    val content: BookContent
)

class BookContent(
    val title: String,
    val paragraphs: List<Paragraph>
)

class BookMetadata(
    val author: String,
    val publishedAt: LocalDate?
)