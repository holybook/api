package app.holybook.import

import app.holybook.import.model.ContentDescriptor
import app.holybook.import.network.Http.client
import app.holybook.import.parsers.BibliothekBahaiDe
import app.holybook.import.parsers.ReferenceLibrary
import app.holybook.lib.db.Database.transactionSuspending
import app.holybook.lib.models.findAuthorIdByName
import app.holybook.lib.models.insertAuthor
import app.holybook.lib.models.insertBook
import app.holybook.lib.models.insertParagraphs
import app.holybook.lib.models.insertTranslation
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import java.io.IOException
import java.sql.Connection
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("import")
val rules = listOf(ReferenceLibrary.rule, BibliothekBahaiDe.rule)

fun parseParagraphs(contentType: ContentType?, url: Url, content: ByteArray): BookContent? {
  for (rule in rules) {
    if (rule.matcher.matches(contentType, url)) {
      return rule.parser(content)
    }
  }

  return null
}

suspend fun fetchContent(descriptor: ContentDescriptor): BookContent {
  val paragraphContent = client.get(descriptor.url)
  val contentType = paragraphContent.contentType()

  return parseParagraphs(contentType, Url(descriptor.url), paragraphContent.body())
    ?: throw IOException("Could not parse content from url ${descriptor.url}")
}

fun Connection.importContent(bookId: String, content: BookContent, descriptor: ContentDescriptor) {
  insertTranslation(bookId, descriptor.language, content.title)
  insertParagraphs(bookId, descriptor.language, content.paragraphs)
}

suspend fun Connection.fetchAndImportContent(bookId: String, descriptor: ContentDescriptor) {
  val fetchResult = fetchContent(descriptor)
  importContent(bookId, fetchResult, descriptor)
}

suspend fun fetchAndImport(descriptor: ContentDescriptor) {
  log.info("Importing from ${descriptor.url}")
  val content = fetchContent(descriptor)
  transactionSuspending {
    val authorId = ensureAuthor(content.author, descriptor.language)
    insertBook(descriptor.id, authorId, content.publishedAt)

    importContent(descriptor.id, content, descriptor)
  }
}

fun Connection.ensureAuthor(name: String, language: String) =
  findAuthorIdByName(name, language) ?: insertAuthor(name, language)
