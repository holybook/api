package app.holybook.import

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.models.dropBooksTable
import app.holybook.lib.models.dropParagraphsTable
import app.holybook.lib.models.dropTranslationsTable
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