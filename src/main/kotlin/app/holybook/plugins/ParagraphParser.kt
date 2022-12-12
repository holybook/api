package app.holybook.plugins

interface ParagraphParser {

    val contentType: String

    fun parse(content: ByteArray): List<String>

}