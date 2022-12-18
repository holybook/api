package app.holybook.import

import app.holybook.api.models.Books
import app.holybook.api.models.Paragraphs
import app.holybook.api.models.Translations
import app.holybook.import.parsers.BibliothekBahaiDe
import app.holybook.import.parsers.PdfParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.*


val client = HttpClient()
val parsers = listOf(PdfParser(), BibliothekBahaiDe.parser)

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

suspend fun fetchContent(contentInfo: ContentInfo): FetchResult {
    val paragraphContent = client.get(contentInfo.url)
    val contentType = paragraphContent.contentType()
    val lastModified = paragraphContent.lastModified()

    val content = parseParagraphs(
        contentType,
        contentInfo.url,
        paragraphContent.body()
    )
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.url}")
    return FetchResult(content, lastModified)
}

fun importContent(
    existingBookId: Int?,
    fetchResult: FetchResult,
    info: ContentInfo
): Int {
    return transaction {
        val bookId = existingBookId
            ?: (Books.insert {
                it[author] = fetchResult.content.author
            } get Books.id).value

        Translations.insert {
            it[Translations.bookId] = bookId
            it[language] = info.language
            it[title] = fetchResult.content.title
            if (fetchResult.lastModified != null) {
                it[lastModified] =
                    Instant.ofEpochMilli(fetchResult.lastModified.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
            }
        }
        fetchResult.content.paragraphs.forEachIndexed { i, paragraph ->
            Paragraphs.insert {
                it[Paragraphs.bookId] = bookId
                it[index] = i
                it[language] = info.language
                it[text] = paragraph.text
                it[type] = paragraph.type
            }
        }
        bookId
    }
}

suspend fun fetchAndImportContent(existingBookId: Int?, info: ContentInfo): Int {
    val fetchResult = fetchContent(info)
    return importContent(existingBookId, fetchResult, info)
}

class FetchResult(val content: BookContent, val lastModified: Date?)

@Serializable
class ContentInfo(val url: String, val language: String)