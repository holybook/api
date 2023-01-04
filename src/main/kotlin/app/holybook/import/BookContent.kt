package app.holybook.import

import app.holybook.api.models.Paragraph

class BookContent(
    val title: String,
    val author: String,
    val paragraphs: List<Paragraph>
)