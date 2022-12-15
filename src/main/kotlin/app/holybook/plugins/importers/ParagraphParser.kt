package app.holybook.plugins.importers

interface ParagraphParser {

    val contentType: String

    fun parse(content: ByteArray): List<String>

}