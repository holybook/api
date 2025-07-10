package app.holybook.eval

import app.holybook.lib.translation.ModelConfiguration
import app.holybook.lib.translation.Translator
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("eval")

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

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    val translator = cmd.translator

    log.info("Initialized translator: ${translator::class.java.simpleName}")
    log.info("Evaluation tool is ready. This is a skeleton implementation.")

    // TODO: Implement evaluation logic
    // 1. Load input data
    // 2. Run translation using the selected translator
    // 3. Evaluate results (accuracy, performance, etc.)
    // 4. Output results
  } catch (e: CommandLineFailed) {
    log.error(e.message)
  }
}

val CommandLine.apiKey: String
  get() =
    getOptionValue("k")
      ?: System.getenv("GEMINI_API_KEY")
      ?: throw CommandLineFailed("API key not found")

val CommandLine.modelName: String
  get() = getOptionValue("m", "gemini-2.5-flash-lite-preview-06-17")

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