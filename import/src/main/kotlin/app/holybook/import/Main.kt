package app.holybook.import

import app.holybook.import.sources.fetchSources
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>) = runBlocking {
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

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  when (cmd.args.firstOrNull() ?: "import") {
    "import" -> import()
    "reset" -> reset()
    "fetch-sources" -> fetchSources()
  }
}

suspend fun import() {
  fetchAndImportIndex()
}

suspend fun reset() {
  resetDatabase()
  import()
}
