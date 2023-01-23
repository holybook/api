package app.holybook.import

import app.holybook.api.db.Database.transaction
import app.holybook.api.db.Database.transactionSuspending
import app.holybook.api.models.getTranslationLastModified
import app.holybook.api.models.insertBook
import app.holybook.api.models.insertParagraphs
import app.holybook.api.models.upsertTranslation
import app.holybook.import.parsers.BibliothekBahaiDe
import app.holybook.import.parsers.ParagraphParser
import app.holybook.import.parsers.PdfParser
import app.holybook.import.parsers.ReferenceLibrary
import app.holybook.util.serialization.DateSerializer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.time.LocalDateTime

val client = HttpClient()
val parsers =
    listOf(PdfParser(), BibliothekBahaiDe.parser)
val originalParsers = listOf(ReferenceLibrary.parser)

fun <T> parseParagraphs(
    parsers: List<ParagraphParser<T>>,
    contentType: ContentType?,
    url: Url,
    content: ByteArray
): T? {
    for (parser in parsers) {
        if (parser.matches(contentType, url)) {
            return parser.parse(content)
        }
    }

    return null
}

suspend fun <T> fetchContent(
    parsers: List<ParagraphParser<T>>,
    contentInfo: ContentInfo
): T {
    val paragraphContent = client.get(contentInfo.url)
    val contentType = paragraphContent.contentType()

    return parseParagraphs(
        parsers,
        contentType,
        Url(contentInfo.url),
        paragraphContent.body()
    )
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.url}")
}

fun Connection.importContent(
    log: Logger,
    bookId: String,
    content: BookContent,
    info: ContentInfo
) {
    val translationLastModified =
        getTranslationLastModified(bookId, info.language)
    if (translationLastModified != null && !translationLastModified.isBefore(
            info.lastModified
        )
    ) {
        log.info("Translation $bookId:${info.language} is already at the newest version.")
        return
    }
    upsertTranslation(
        bookId,
        info.language,
        content.title,
        info.lastModified
    )
    insertParagraphs(bookId, info.language, content.paragraphs)
}

suspend fun Connection.fetchAndImportContent(
    log: Logger,
    bookId: String,
    info: ContentInfo
) {
    val fetchResult = fetchContent(parsers, info)
    importContent(log, bookId, fetchResult, info)
}

suspend fun fetchAndImportBook(
    log: Logger,
    bookInfo: BookInfo
) {
    log.info("Importing from ${bookInfo.original.url}")
    val original = fetchContent(originalParsers, bookInfo.original)
    transactionSuspending {
        insertBook(bookInfo.id, original.metadata.author)
        importContent(log, bookInfo.id, original.content, bookInfo.original)
        bookInfo.translations.forEach {
            log.info("Importing from ${it.url}")
            fetchAndImportContent(log, bookInfo.id, it)
        }
    }
}

@Serializable
class ContentInfo(
    val url: String,
    val language: String,
    @Serializable(with = DateSerializer::class) val lastModified: LocalDateTime,
)
