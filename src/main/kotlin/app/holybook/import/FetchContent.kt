package app.holybook.import

import app.holybook.api.models.Books
import app.holybook.api.models.Paragraphs
import app.holybook.api.models.Translations
import app.holybook.import.parsers.BibliothekBahaiDe
import app.holybook.import.parsers.PdfParser
import app.holybook.import.parsers.ReferenceLibrary
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.*


val client = HttpClient()
val parsers =
    listOf(PdfParser(), ReferenceLibrary.parser, BibliothekBahaiDe.parser)

fun parseParagraphs(
    contentType: ContentType?, url: Url, content: ByteArray
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
        Url(contentInfo.url),
        paragraphContent.body()
    )
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.url}")
    return FetchResult(content, lastModified)
}

fun importContent(
    log: Logger,
    bookId: String,
    fetchResult: FetchResult,
    info: ContentInfo
) {
    transaction {
        Books.insertIgnore {
            it[id] = bookId
            it[author] = fetchResult.content.author
        }

        val translationInsertResult = Translations.insertIgnore {
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
        if (translationInsertResult.insertedCount == 0) {
            log.info("Skipping $bookId:${info.language} since it is already present.")
            return@transaction
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

    }
}

suspend fun fetchAndImportContent(
    log: Logger,
    bookId: String,
    info: ContentInfo
) {
    val fetchResult = fetchContent(info)
    importContent(log, bookId, fetchResult, info)
}

class FetchResult(val content: BookContent, val lastModified: Date?)

@Serializable
class ContentInfo(val url: String, val language: String)