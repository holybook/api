package app.holybook.import

import app.holybook.api.models.Books
import app.holybook.api.models.Paragraphs
import app.holybook.api.models.Translations
import app.holybook.api.models.Translations.lastModified
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
import java.time.LocalDateTime
import java.util.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

val client = HttpClient()
val parsers = listOf(PdfParser(), ReferenceLibrary.parser, BibliothekBahaiDe.parser)

fun parseParagraphs(contentType: ContentType?, url: Url, content: ByteArray): BookContent? {
  for (parser in parsers) {
    if (parser.matches(contentType, url)) {
      return parser.parse(content)
    }
  }

  return null
}

suspend fun fetchContent(contentInfo: ContentInfo): BookContent {
  val paragraphContent = client.get(contentInfo.url)
  val contentType = paragraphContent.contentType()

  return parseParagraphs(contentType, Url(contentInfo.url), paragraphContent.body())
      ?: throw InvalidBodyException("Could not parse content from url ${contentInfo.url}")
}

fun importContent(log: Logger, bookId: String, content: BookContent, info: ContentInfo) {
  transaction {
    Books.insertIgnore {
      it[id] = bookId
      it[author] = content.author
    }

    val existingTranslation =
        Translations.select {
              (Translations.bookId eq bookId) and (Translations.language eq info.language)
            }
            .firstOrNull()

    if (existingTranslation != null &&
        !existingTranslation[lastModified].isBefore(info.lastModified)) {
      log.info("Translation $bookId:${info.language} is already at the newest version.")
      return@transaction
    }

    if (existingTranslation != null) {
      log.info("Updating translation $bookId:${info.language}.")
      Translations.update { it[lastModified] = info.lastModified }
      Paragraphs.deleteWhere { (Paragraphs.bookId eq bookId) and (language eq info.language) }
    } else {
      log.info("Adding new translation $bookId:${info.language}.")
      Translations.insert {
        it[Translations.bookId] = bookId
        it[language] = info.language
        it[title] = content.title
        it[lastModified] = info.lastModified
      }
    }
    content.paragraphs.forEachIndexed { i, paragraph ->
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

suspend fun fetchAndImportContent(log: Logger, bookId: String, info: ContentInfo) {
  val fetchResult = fetchContent(info)
  importContent(log, bookId, fetchResult, info)
}

@Serializable
class ContentInfo(
    val url: String,
    val language: String,
    @Serializable(with = DateSerializer::class) val lastModified: LocalDateTime
)
