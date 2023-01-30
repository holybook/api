package app.holybook.import.parsers

import io.ktor.http.*

interface ContentParser<T> {

    fun matches(contentType: ContentType?, url: Url): Boolean

    fun parse(content: ByteArray): T

}