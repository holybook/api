package app.holybook.import

import app.holybook.api.db.Database.transaction
import app.holybook.api.models.getTranslationLastModified
import app.holybook.api.models.insertBook
import app.holybook.api.models.insertParagraphs
import app.holybook.api.models.upsertTranslation
import app.holybook.import.parsers.BibliothekBahaiDe
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
import java.time.LocalDateTime
import java.util.*

val client = HttpClient()
val parsers =
    listOf(PdfParser(), ReferenceLibrary.parser, BibliothekBahaiDe.parser)
val originalParsers = listOf(ReferenceLibrary.parser)

fun parseParagraphs(
    contentType: ContentType?,
    url: Url,
    content: ByteArray,
    isOriginal: Boolean
): BookContent? {
    val parsers = if (isOriginal) originalParsers else parsers

    for (parser in parsers) {
        if (parser.matches(contentType, url)) {
            return parser.parse(content)
        }
    }

    return null
}

suspend fun fetchContent(
    contentInfo: ContentInfo,
    isOriginal: Boolean
): BookContent {
    val paragraphContent = client.get(contentInfo.url)
    val contentType = paragraphContent.contentType()

    return parseParagraphs(
        contentType,
        Url(contentInfo.url),
        paragraphContent.body(),
        isOriginal
    )
        ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.url}")
}

fun importContent(
    log: Logger,
    bookId: String,
    content: BookContent,
    info: ContentInfo
) {
    transaction {
        insertBook(bookId, content.author)
        val translationLastModified =
            getTranslationLastModified(bookId, info.language)
        if (translationLastModified != null && !translationLastModified.isBefore(
                info.lastModified
            )
        ) {
            log.info("Translation $bookId:${info.language} is already at the newest version.")
            return@transaction
        }
        upsertTranslation(
            bookId,
            info.language,
            content.title,
            info.lastModified
        )
        insertParagraphs(bookId, info.language, content.paragraphs)
    }
}

suspend fun fetchAndImportContent(
    log: Logger,
    bookId: String,
    info: ContentInfo,
    isOriginal: Boolean
) {
    val fetchResult = fetchContent(info, isOriginal)
    importContent(log, bookId, fetchResult, info)
}

@Serializable
class ContentInfo(
    val url: String,
    val language: String,
    @Serializable(with = DateSerializer::class) val lastModified: LocalDateTime,
)
