package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.TranslateResponse
import app.holybook.lib.models.translate
import com.google.genai.Client
import org.slf4j.LoggerFactory

object Translation {

  private val log = LoggerFactory.getLogger("translation")
  val client = Client()

  val TranslateResponse.id: String
    get() = "${allOriginalResults[0].bookId}:${translatedParagraph.index}"

  fun translate(
    fromLanguage: String,
    toLanguage: String,
    text: String
  ): String {
    val paragraphs =
      text.lines().mapNotNull { it.trim().takeIf { it.isNotBlank() } }

    val translationPairs =
      paragraphs.map { paragraphText ->
        val translateResponse = translate(
          TranslateRequest(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            text = paragraphText,
          )
        )
        TranslationPair(
          authoritativeTranslation = translateResponse,
          textToBeTranslated = ParagraphToBeTranslated(
            translateResponse?.id,
            paragraphText
          ),
        )
      }

    val prompt = TranslationTemplateEngine.renderPrompt(
      TranslationTemplateData(
        fromLanguage,
        toLanguage,
        translationPairs.mapNotNull { it.authoritativeTranslation },
        translationPairs.map { it.textToBeTranslated }
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

data class TranslationPair(
  val authoritativeTranslation: TranslateResponse?,
  val textToBeTranslated: ParagraphToBeTranslated
)
