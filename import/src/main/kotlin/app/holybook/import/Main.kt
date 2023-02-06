package app.holybook.import

import app.holybook.import.sources.SourceFetcher.fetchSources
import app.holybook.lib.db.Database
import app.holybook.lib.models.readContentDescriptors
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.nio.file.Files
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

fun main(args: Array<String>): Unit = runBlocking {
  val options = Options()
  options.addOption(
    "r",
    "recreate-db",
    false,
    "Recreate the database and populate it from scratch."
  )
  options.addOption("h", "host", true, "Database host")
  options.addOption("p", "port", true, "Database port")
  options.addOption("d", "database", true, "Database name")
  options.addOption("u", "user", true, "Database username")
  options.addOption("pwd", "password", false, "Use password")
  options.addOption("i", "input", true, "Input directory")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  Database.init(cmd.getJdbcUrl())
  if (cmd.hasOption("r")) {
    resetDatabase()
  }
  createDatabase()


}

fun processPath(path: Path) {
  if (path.isDirectory()) {
    Files.list(path).forEach { processPath(it) }
    return
  }

  runBlocking {
    val descriptors = readContentDescriptors(path.inputStream())
    log.info("Processing ${descriptors.size} descriptors from ${path.fileName}")
    fetchAll(descriptors, outputDirectory)
  }
}

fun CommandLine.getCacheDirectory(): Path =
  FileSystems.getDefault().getPath(getOptionValue("c", "cache"))

fun CommandLine.getJdbcUrl() = getJdbcUrl(
  host = getOptionValue("h", "127.0.0.1"),
  port = getOptionValue("p", "5432"),
  database = getOptionValue("d", "holybook"),
  user = getOptionValue("u", "server"),
  usePassword = hasOption("pwd")
)