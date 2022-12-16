package app.holybook.tools.importers

interface ParagraphParser {

    val contentType: String

    fun parse(content: ByteArray): List<String>

}