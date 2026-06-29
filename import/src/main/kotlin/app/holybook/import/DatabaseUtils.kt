package app.holybook.import

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.models.createBooksTable
import app.holybook.lib.models.createParagraphsTable
import app.holybook.lib.models.createTranslationsTable
import app.holybook.lib.models.dropBooksTable
import app.holybook.lib.models.dropParagraphsTable
import app.holybook.lib.models.dropTranslationsTable
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("db")

fun createDatabase() {
  log.info("Creating database tables")
  transaction {
    // Create tables:
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
  }
}

/**
 * Tracks which content commit is currently imported. Lives outside the content
 * tables (it is not dropped by [resetDatabase]) so the sync job can compare the
 * repository HEAD against what was actually imported, and is only advanced after
 * a successful import — making the sync self-healing if an import fails midway.
 */
fun createSyncStateTable() {
  transaction {
    createStatement()
      .executeUpdate(
        "CREATE TABLE IF NOT EXISTS sync_state (key TEXT PRIMARY KEY, value TEXT NOT NULL)"
      )
  }
}

fun recordAppliedCommit(commit: String) {
  log.info("Recording applied commit $commit")
  transaction {
    val statement =
      prepareStatement(
        """
        INSERT INTO sync_state(key, value) VALUES ('applied_commit', ?)
        ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value
        """
          .trimIndent()
      )
    statement.setString(1, commit)
    statement.executeUpdate()
  }
}

fun buildJdbcUrl(host: String, port: String, database: String): String {
  // Credentials are supplied separately to Database.init, not in the URL.
  return "jdbc:postgresql://$host:$port/$database"
}

fun readPassword(): String {
  val console = System.console()
  if (console == null) {
    print("Password: ")
    return readln()
  }
  return String(console.readPassword("Password: "))
}
