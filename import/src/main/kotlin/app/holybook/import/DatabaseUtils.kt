package app.holybook.import

import app.holybook.lib.db.Database
import app.holybook.lib.db.Database.transaction
import app.holybook.lib.models.createAuthorTables
import app.holybook.lib.models.createBooksTable
import app.holybook.lib.models.createParagraphsTable
import app.holybook.lib.models.createTranslationsTable
import app.holybook.lib.models.dropAuthorTables
import app.holybook.lib.models.dropBooksTable
import app.holybook.lib.models.dropParagraphsTable
import app.holybook.lib.models.dropTranslationsTable
import org.apache.commons.cli.CommandLine
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

fun initDb(cmd: CommandLine) {
  val jdbcUrl =
    getJdbcUrl(
      host = cmd.getOptionValue("h", "127.0.0.1"),
      port = cmd.getOptionValue("p", "5432"),
      database = cmd.getOptionValue("d", "holybook"),
      user = cmd.getOptionValue("u", "server"),
      usePassword = cmd.hasOption("pwd")
    )
  Database.init(jdbcUrl)
}

fun getJdbcUrl(
  host: String,
  port: String,
  database: String,
  user: String,
  usePassword: Boolean
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
