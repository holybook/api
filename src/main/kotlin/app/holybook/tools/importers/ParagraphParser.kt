package app.holybook.tools.importers

import app.holybook.tools.BookContent
import io.ktor.http.*

interface ParagraphParser {

    fun matches(contentType: ContentType?, url: String): Boolean

    fun parse(content: ByteArray): BookContent

}