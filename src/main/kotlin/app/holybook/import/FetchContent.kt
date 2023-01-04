package app.holybook.import

import app.holybook.api.db.Database
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
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlinx.serialization.Serializable

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
  Database.transaction { connection ->

    val preparedStatement = connection.prepareStatement("""
      INSERT INTO books(id, author) VALUES (?, ?) ON CONFLICT DO NOTHING
    """.trimIndent())

    preparedStatement.setString(1, bookId)
    preparedStatement.setString(2, content.author)
    preparedStatement.executeUpdate()

    val getExistingTranslation = connection.prepareStatement("""
      SELECT last_modified FROM translations WHERE book = ? AND language = ?
    """.trimIndent())
    getExistingTranslation.setString(1, bookId)
    getExistingTranslation.setString(2, info.language)
    val existingTranslation = getExistingTranslation.executeQuery()

    if (existingTranslation.next()) {
      if (!existingTranslation.getTimestamp("last_modified").toLocalDateTime()
          .isBefore(info.lastModified)
      ) {
        log.info("Translation $bookId:${info.language} is already at the newest version.")
        return@transaction
      }
    }

    val upsertTranslation = connection.prepareStatement("""
      INSERT INTO translations(book, language, title, last_modified) VALUES (?, ?, ?, ?)
      ON CONFLICT (book, language) DO UPDATE SET 
        title = ?,
        last_modified = ? 
    """.trimIndent())
    upsertTranslation.setString(1, bookId)
    upsertTranslation.setString(2, info.language)
    upsertTranslation.setString(3, content.title)
    upsertTranslation.setTimestamp(4, Timestamp.valueOf(info.lastModified))
    upsertTranslation.setString(5, content.title)
    upsertTranslation.setTimestamp(6, Timestamp.valueOf(info.lastModified))
    upsertTranslation.executeUpdate()

    val insertParagraph = connection.prepareStatement("""
        INSERT INTO paragraphs(book, language, index, type, text)
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent())

    content.paragraphs.forEachIndexed { i, paragraph ->
      insertParagraph.setString(1, bookId)
      insertParagraph.setString(2, info.language)
      insertParagraph.setInt(3, i)
      insertParagraph.setString(4, paragraph.type)
      insertParagraph.setString(5, paragraph.text)
      insertParagraph.addBatch()
    }
    insertParagraph.executeBatch()

  }

  // transaction {
  //   Books.insertIgnore {
  //     it[id] = bookId
  //     it[author] = content.author
  //   }
  //
  //   val existingTranslation =
  //       Translations.select {
  //             (Translations.bookId eq bookId) and (Translations.language eq info.language)
  //           }
  //           .firstOrNull()
  //
  //   if (existingTranslation != null &&
  //       !existingTranslation[lastModified].isBefore(info.lastModified)) {
  //     log.info("Translation $bookId:${info.language} is already at the newest version.")
  //     return@transaction
  //   }
  //
  //   if (existingTranslation != null) {
  //     log.info("Updating translation $bookId:${info.language}.")
  //     Translations.update { it[lastModified] = info.lastModified }
  //     Paragraphs.deleteWhere { (Paragraphs.bookId eq bookId) and (language eq info.language) }
  //   } else {
  //     log.info("Adding new translation $bookId:${info.language}.")
  //     Translations.insert {
  //       it[Translations.bookId] = bookId
  //       it[language] = info.language
  //       it[title] = content.title
  //       it[lastModified] = info.lastModified
  //     }
  //   }
  //   content.paragraphs.forEachIndexed { i, paragraph ->
  //     Paragraphs.insert {
  //       it[Paragraphs.bookId] = bookId
  //       it[index] = i
  //       it[language] = info.language
  //       it[text] = paragraph.text
  //       it[type] = paragraph.type
  //     }
  //   }
  // }
}

suspend fun fetchAndImportContent(log: Logger, bookId: String, info: ContentInfo) {
  val fetchResult = fetchContent(info)
  importContent(log, bookId, fetchResult, info)
}

@Serializable
class ContentInfo(
  val url: String,
  val language: String,
  @Serializable(with = DateSerializer::class) val lastModified: LocalDateTime,
)
