package app.holybook.import

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.models.createAuthorsTable
import app.holybook.lib.models.createBooksTable
import app.holybook.lib.models.createParagraphsTable
import app.holybook.lib.models.createTranslationsTable
import app.holybook.lib.models.dropAuthorsTable
import app.holybook.lib.models.dropBooksTable
import app.holybook.lib.models.dropParagraphsTable
import app.holybook.lib.models.dropTranslationsTable
import app.holybook.lib.models.seedAuthors
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("db")

fun createDatabase() {
  log.info("Creating database tables")
  transaction {
    // Create tables:
    createAuthorsTable()
    createBooksTable()
    createTranslationsTable()
    createParagraphsTable()

    // Seed the known authors so books can reference them.
    seedAuthors()
  }
}

fun resetDatabase() {
  log.info("Resetting database")
  transaction {
    dropParagraphsTable()
    dropTranslationsTable()
    dropBooksTable()
    dropAuthorsTable()
  }
}

fun getJdbcUrl(
  host: String,
  port: String,
  database: String,
  user: String,
  usePassword: Boolean,
): String {
  val passwordQuery =
    if (usePassword) {
      "&password=${readPassword()}"
    } else {
      ""
    }
  return "jdbc:postgresql://$host:$port/$database?user=$user$passwordQuery"
}

private fun readPassword(): String {
  val console = System.console()
  if (console == null) {
    print("Password: ")
    return readln()
  }
  return String(console.readPassword("Password: "))
}
