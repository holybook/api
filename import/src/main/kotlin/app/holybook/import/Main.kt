package app.holybook.import

import app.holybook.import.ImportLogger.log
import app.holybook.lib.db.Database
import app.holybook.lib.models.toBookContent
import app.holybook.lib.parsers.readDocument
import app.holybook.lib.path.PathExtensions.listFilesRecursive
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>): Unit = runBlocking {
  val options = Options()
  options.addOption("jdbc", true, "Full custom jdbc url")
  options.addOption("h", "host", true, "Database host")
  options.addOption("p", "port", true, "Database port")
  options.addOption("d", "database", true, "Database name")
  options.addOption("u", "user", true, "Database username")
  options.addOption("pwd", "password", false, "Use password")
  options.addOption("i", "input", true, "Input directory")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  Database.init(cmd.getJdbcUrl())
  resetDatabase()
  createDatabase()

  cmd.getInputDirectory().listFilesRecursive().forEach { processFile(it) }
}

fun processFile(path: Path) {
  log.info("Importing ${path.toAbsolutePath()}")
  val content = path.inputStream().readDocument().toBookContent()
  runBlocking { importContent(content) }
}

fun CommandLine.getInputDirectory(): Path =
  FileSystems.getDefault().getPath(getOptionValue("i", "../../data/content"))

fun CommandLine.getJdbcUrl(): String {
  if (hasOption("jdbc")) {
    return getOptionValue("jdbc")
  }

  return getJdbcUrl(
    host = getOptionValue("h", "127.0.0.1"),
    port = getOptionValue("p", "5432"),
    database = getOptionValue("d", "holybook"),
    user = getOptionValue("u", "server"),
    usePassword = hasOption("pwd"),
  )
}
