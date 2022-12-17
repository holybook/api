package app.holybook.tools

import app.holybook.api.models.Paragraphs
import app.holybook.tools.importers.BibliothekBahaiDe
import app.holybook.tools.importers.PdfParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

val client = HttpClient()
val parsers = listOf(PdfParser(), BibliothekBahaiDe.parser)

fun main(args: Array<String>) {
    runBlocking {
        fetchContent(args[1].toInt(), ContentInfo(args[3], args[2]))
    }
}

fun parseParagraphs(
    contentType: ContentType?, url: String, content: ByteArray
): BookContent? {
    for (parser in parsers) {
        if (parser.matches(contentType, url)) {
            return parser.parse(content)
        }
    }

    return null
}

suspend fun fetchContent(bookId: Int, contentInfo: ContentInfo) {
    val paragraphContent = client.get(contentInfo.sourceUrl)
    val contentType = paragraphContent.contentType()

    val bookContent = parseParagraphs(
        contentType,
        contentInfo.sourceUrl,
        paragraphContent.body()
    )
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.sourceUrl}")
    transaction {
        bookContent.paragraphs.forEachIndexed { i, paragraph ->
            Paragraphs.insert {
                it[Paragraphs.bookId] = bookId
                it[index] = i
                it[language] = contentInfo.language
                it[text] = paragraph.text
                it[type] = paragraph.type
            }
        }
    }
}

class ContentInfo(val sourceUrl: String, val language: String)