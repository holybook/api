package app.holybook.tools

import app.holybook.api.models.Paragraphs
import app.holybook.tools.importers.PdfParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

val client = HttpClient()
val parsers = listOf(PdfParser())

fun parseParagraphs(
    contentType: ContentType?, content: ByteArray
): List<String>? {
    for (parser in parsers) {
        if (contentType?.match(parser.contentType) == true) {
            return parser.parse(content)
        }
    }

    return null
}

suspend fun fetchContent(bookId: Int, contentInfo: ContentInfo) {
    val paragraphContent = client.get(contentInfo.sourceUrl)
    val contentType = paragraphContent.contentType()

    val paragraphs = parseParagraphs(contentType, paragraphContent.body())
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.sourceUrl}")
    transaction {
        paragraphs.forEachIndexed { i, paragraph ->
            Paragraphs.insert {
                it[Paragraphs.bookId] = bookId
                it[index] = i
                it[language] = contentInfo.language
                it[text] = paragraph
            }
        }
    }
}

class ContentInfo(val sourceUrl: String, val language: String)