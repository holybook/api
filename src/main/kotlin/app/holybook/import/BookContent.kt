package app.holybook.import

class BookContent(
    val title: String,
    val author: String,
    val paragraphs: List<Paragraph>
)

class Paragraph(val text: String, val type: String)