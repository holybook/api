package app.holybook.index

import app.holybook.lib.db.Database
import app.holybook.lib.path.PathExtensions.listFilesRecursive
import java.nio.file.FileSystems
import java.nio.file.Path
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options

fun main(args: Array<String>) {
  val options = Options()
  options.addOption("i", "input", true, "Input directory")
  options.addOption("o", "output", true, "Output file")

  val parser = DefaultParser()
  val cmd = parser.parse(options, args)

  Database.init("jdbc:sqlite:${cmd.getOutputFile()}")

  val indexer = Indexer()
  indexer.indexDirectoryRecursive(cmd.getInputDirectory())
}

fun CommandLine.getInputDirectory(): Path =
  FileSystems.getDefault().getPath(getOptionValue("i", "raw/content"))

fun CommandLine.getOutputFile(): Path =
  FileSystems.getDefault().getPath(getOptionValue("o", "raw/index.json"))