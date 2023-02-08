package app.holybook.import

import app.holybook.lib.db.Database.transactionSuspending
import app.holybook.lib.models.BookContent
import app.holybook.lib.models.insertBook
import app.holybook.lib.models.insertParagraphs
import app.holybook.lib.models.insertTranslation
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("import")

suspend fun importContent(book: BookContent) {
  try {
    transactionSuspending {
      insertBook(book.id, book.author, book.publishedAt)
      insertTranslation(book.id, book.language, book.title)
      insertParagraphs(book.id, book.language, book.paragraphs)
    }
  } catch (e: Throwable) {
    log.warn("Failed to fetch and import ${book.id}", e)
  }
}
