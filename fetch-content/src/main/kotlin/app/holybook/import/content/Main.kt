package app.holybook.import.content

import app.holybook.lib.models.readContentDescriptors
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("fetch-content")

fun main(args: Array<String>): Unit = runBlocking {
  val options = Options()
  options.addOption("i", "input", true, "Input directory containing content descriptor files")
  options.addOption("o", "output", true, "Target directory for content")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  val inputPath = FileSystems.getDefault().getPath(cmd.getOptionValue("i", "../../data/index"))
  val outputDirectory = FileSystems.getDefault().getPath(cmd.getOptionValue("o", "../../data/content"))
  processPath(inputPath, outputDirectory)
}

fun processPath(path: Path, outputDirectory: Path) {
  if (path.isDirectory()) {
    Files.list(path).forEach { processPath(it, outputDirectory) }
    return
  }

  runBlocking {
    log.info("Reading descriptors from ${path.fileName}")
    val descriptors = readContentDescriptors(path.inputStream())
    log.info("Processing ${descriptors.size} descriptors from ${path.fileName}")
    fetchAll(descriptors, outputDirectory)
  }
}
