package app.holybook.import.parsers

import app.holybook.import.BookContent
import io.ktor.http.*

interface ParagraphParser {

    fun matches(contentType: ContentType?, url: String): Boolean

    fun parse(content: ByteArray): BookContent

}