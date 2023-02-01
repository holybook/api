package app.holybook.import

import app.holybook.lib.models.Paragraph
import java.time.LocalDate

class BookContent(
    val title: String,
    val author: String,
    val publishedAt: LocalDate?,
    val paragraphs: List<Paragraph>
)