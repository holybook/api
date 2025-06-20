package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.translate
import com.google.genai.Client
import org.slf4j.LoggerFactory

object Translation {

  private val log = LoggerFactory.getLogger("translation")
  val client = Client()

  fun translate(
    fromLanguage: String,
    toLanguage: String,
    text: String
  ): String {
    val paragraphs = text.lines()

    val translateResponses =
      paragraphs.mapNotNull { paragraphText ->
        translate(
          TranslateRequest(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            text = paragraphText,
          )
        )
      }

    val prompt = TranslationTemplateEngine.renderPrompt(
      TranslationTemplateData(
        translateResponses
      )
    )

    log.info("Prompt: $prompt")

    return client.models.generateContent(
      "gemini-2.5-flash",
      prompt,
      null
    ).text() ?: "No response"

  }
}
