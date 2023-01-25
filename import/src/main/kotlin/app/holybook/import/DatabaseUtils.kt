package app.holybook.import

import app.holybook.api.db.Database.transaction
import app.holybook.api.models.dropBooksTable
import app.holybook.api.models.dropParagraphsTable
import app.holybook.api.models.dropTranslationsTable
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("db")

fun resetDatabase() {
  log.info("Resetting database")
  transaction {
    dropParagraphsTable()
    dropTranslationsTable()
    dropBooksTable()
  }
}