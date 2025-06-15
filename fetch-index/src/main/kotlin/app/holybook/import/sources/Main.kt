package app.holybook.import.sources

import app.holybook.import.sources.SourceFetcher.fetchSources
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>): Unit = runBlocking {
  val options = Options()
  options.addOption("o", "output", true, "Target directory for source indices")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  fetchSources(cmd.getOutputDirectory())
}

fun CommandLine.getOutputDirectory(): Path =
  FileSystems.getDefault().getPath(getOptionValue("o", "../../data/index"))
