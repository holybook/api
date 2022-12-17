package app.holybook.tools

class BookContent(
    val title: String,
    val paragraphs: List<Paragraph>
)

class Paragraph(val text: String, val type: String)