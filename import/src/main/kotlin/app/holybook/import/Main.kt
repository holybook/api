package app.holybook.import

import app.holybook.import.sources.SourceFetcher.fetchSources
import app.holybook.lib.db.Database
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>): Unit = runBlocking {
  val options = Options()
  options.addOption(
    "c",
    "recreate-db",
    false,
    "Recreate the database and populate it from scratch."
  )
  options.addOption("h", "host", true, "Database host")
  options.addOption("p", "port", true, "Database port")
  options.addOption("d", "database", true, "Database name")
  options.addOption("u", "user", true, "Database username")
  options.addOption("pwd", "password", false, "Use password")
  options.addOption("o", "output", true, "Target directory for source indices")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  when (cmd.args.firstOrNull() ?: "import") {
    "import" -> import(cmd.getJdbcUrl())
    "reset" -> reset(cmd.getJdbcUrl())
    "fetch-sources" -> fetchSources(cmd.getTargetDirectory())
    else -> import(cmd.getJdbcUrl())
  }
}

suspend fun import(jdbcUrl: String) {
  Database.init(jdbcUrl)
  fetchAndImportIndex()
}

suspend fun reset(jdbcUrl: String) {
  Database.init(jdbcUrl)
  resetDatabase()
  fetchAndImportIndex()
}

fun CommandLine.getTargetDirectory(): Path =
  FileSystems.getDefault().getPath(getOptionValue("o", "cache"))

fun CommandLine.getJdbcUrl() = getJdbcUrl(
  host = getOptionValue("h", "127.0.0.1"),
  port = getOptionValue("p", "5432"),
  database = getOptionValue("d", "holybook"),
  user = getOptionValue("u", "server"),
  usePassword = hasOption("pwd")
)