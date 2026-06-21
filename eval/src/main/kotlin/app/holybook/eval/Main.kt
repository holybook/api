package app.holybook.eval

import app.holybook.lib.translation.ModelConfiguration
import app.holybook.lib.translation.ParagraphWithReference
import app.holybook.lib.translation.Translator
import app.holybook.lib.translation.TranslationModelRequest
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("eval")
private val json = Json { ignoreUnknownKeys = true }

fun main(args: Array<String>): Unit = runBlocking {
  try {
    val options = Options()
    options.addOption("k", "api-key", true, "API key for translation services")
    options.addOption("m", "model", true, "Model name")
    options.addOption("i", "input", true, "Input file or directory")
    options.addOption(
      "t",
      "translator",
      true,
      "Translator implementation to use (monolithic, incremental)",
    )
    options.addOption(
      "p",
      "paragraph-translator",
      true,
      "Paragraph translator implementation. Use this when not setting translator.",
    )
    options.addOption(
      "x",
      "text-matcher",
      true,
      "Text matcher implementation. Use this when not setting translator.",
    )
    options.addOption("n", "runs", true, "Number of times to repeat the evaluation (default: 1)")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    val translator = cmd.translator
    log.info("Initialized translator: ${translator::class.java.simpleName}")

    val inputPath = cmd.getOptionValue("i") ?: throw CommandLineFailed("Input file is required (-i)")
    val inputFile = File(inputPath)
    val runs = cmd.getOptionValue("n", "1").toIntOrNull()?.takeIf { it > 0 }
      ?: throw CommandLineFailed("Number of runs must be a positive integer")

    val files =
      when {
        inputFile.isDirectory ->
          inputFile.listFiles { f -> f.extension == "json" }?.toList() ?: emptyList()
        inputFile.exists() -> listOf(inputFile)
        else -> throw CommandLineFailed("Input not found: $inputPath")
      }

    if (files.isEmpty()) throw CommandLineFailed("No JSON files found at: $inputPath")

    val total = files.size * runs
    var completed = 0
    var grandTotalVerbatim = 0
    var grandTotalScored = 0

    printProgress(completed, total, grandTotalVerbatim, grandTotalScored)

    for (file in files) {
      repeat(runs) {
        val (verbatim, scored) = evaluateFile(file, translator)
        grandTotalVerbatim += verbatim
        grandTotalScored += scored
        completed++
        printProgress(completed, total, grandTotalVerbatim, grandTotalScored)
      }
    }

    val rate = if (grandTotalScored > 0) 100.0 * grandTotalVerbatim / grandTotalScored else 0.0
    print("\r\u001B[K")
    println("Files: ${files.size}  Runs: $runs  |  Verbatim: $grandTotalVerbatim / $grandTotalScored  (${"%.1f".format(rate)}%)")
  } catch (e: CommandLineFailed) {
    log.error(e.message)
  }
}

private fun evaluateFile(file: File, translator: Translator): Pair<Int, Int> {
  val input = json.decodeFromString<EvalInput>(file.readText())
  val referenceById = input.referenceParagraphs.associateBy { it.id }

  val paragraphs =
    input.paragraphs.map { p ->
      ParagraphWithReference(text = p.text, reference = p.id?.let { referenceById[it] })
    }

  val request =
    TranslationModelRequest(
      fromLanguage = input.fromLanguage,
      toLanguage = input.toLanguage,
      paragraphs = paragraphs,
    )

  val response = translator.translate(request)

  var verbatimCount = 0
  var scoredCount = 0

  response.paragraphs.forEachIndexed { i, translated ->
    val inputParagraph = input.paragraphs.getOrNull(i)
    val reference = inputParagraph?.id?.let { referenceById[it] }
    if (reference != null) {
      if (reference.text.contains(translated.text.trim())) verbatimCount++
      scoredCount++
    }
  }

  return Pair(verbatimCount, scoredCount)
}

private fun printProgress(completed: Int, total: Int, verbatim: Int, scored: Int) {
  val barWidth = 24
  val filled = if (total > 0) completed * barWidth / total else 0
  val bar = "█".repeat(filled) + "░".repeat(barWidth - filled)
  val pct = if (total > 0) 100.0 * completed / total else 0.0
  val verbatimStr =
    if (scored > 0) "  Verbatim: $verbatim/$scored (${"%.0f".format(100.0 * verbatim / scored)}%)"
    else ""
  print("\r[$bar] $completed/$total (${"%.0f".format(pct)}%)$verbatimStr\u001B[K")
}

private fun String.truncate(max: Int) = if (length <= max) this else take(max) + "…"

val CommandLine.apiKey: String
  get() =
    getOptionValue("k")
      ?: System.getenv("GEMINI_API_KEY")
      ?: throw CommandLineFailed("API key not found")

val CommandLine.modelName: String
  get() = getOptionValue("m", "gemini-3.1-flash-lite")

val CommandLine.modelConfiguration: ModelConfiguration
  get() = ModelConfiguration(apiKey, modelName)

val CommandLine.translatorRegistry: TranslatorRegistry
  get() =
    DaggerEvalComponent.builder()
      .modelConfiguration(modelConfiguration)
      .build()
      .translatorRegistry()

val CommandLine.translator: Translator
  get() = translatorRegistry.resolve(getOptionValue("t"), getOptionValue("p"), getOptionValue("x"))