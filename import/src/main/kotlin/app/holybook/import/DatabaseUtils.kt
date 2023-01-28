package app.holybook.import

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.models.createAuthorTables
import app.holybook.lib.models.createBooksTable
import app.holybook.lib.models.createParagraphsTable
import app.holybook.lib.models.createTranslationsTable
import app.holybook.lib.models.dropAuthorTables
import app.holybook.lib.models.dropBooksTable
import app.holybook.lib.models.dropParagraphsTable
import app.holybook.lib.models.dropTranslationsTable
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("db")

fun createDatabase() {
  log.info("Creating database tables")
  transaction {
    // Create tables:
    createAuthorTables()
    createBooksTable()
    createTranslationsTable()
    createParagraphsTable()
  }
}

fun resetDatabase() {
  log.info("Resetting database")
  transaction {
    dropParagraphsTable()
    dropTranslationsTable()
    dropBooksTable()
    dropAuthorTables()
  }
}
