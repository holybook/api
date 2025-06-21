package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.TranslateResponse
import app.holybook.lib.models.translate
import com.google.genai.Client
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

object Translation {

  private val log = LoggerFactory.getLogger("translation")
  val client = Client()

  fun translate(
    fromLanguage: String,
    toLanguage: String,
    text: String
  ): TranslationResponse? {
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
          textToBeTranslated = ParagraphWithReference(
            translateResponse?.let {
              ParagraphReference(
                it.bookId,
                it.translatedParagraph.index
              )
            },
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

    val modelResponse = client.models.generateContent(
      "gemini-2.5-flash",
      prompt,
      null
    )

    val modelResponseText = modelResponse.text()

    if (modelResponseText == null) {
      return null
    }

    log.info("Model response: $modelResponseText")

    val response =
      Json.decodeFromString<TranslationModelResponse>(modelResponseText)
    val authoritativeTranslationsMap = translationPairs.mapNotNull {
      if (it.authoritativeTranslation == null) {
        return@mapNotNull null
      }

      it.textToBeTranslated.reference!! to it.authoritativeTranslation
    }.toMap()

    response.validate(authoritativeTranslationsMap)

    return TranslationResponse(paragraphs = response.paragraphs.map { paragraph ->
      val authoritativeTranslation =
        paragraph.reference?.let { authoritativeTranslationsMap[it] }
      ParagraphWithAnnotation(
        annotation = authoritativeTranslation?.annotation,
        text = paragraph.text
      )
    })
  }

  private fun TranslationModelResponse.validate(authoritativeTranslations: Map<ParagraphReference, TranslateResponse>) {
    for (paragraph in paragraphs) {
      val reference = paragraph.reference ?: continue
      val authoritativeText =
        authoritativeTranslations[reference]?.translatedParagraph?.text
          ?: continue
      if (!authoritativeText.contains(paragraph.text)) {
        throw IllegalStateException(
          "Translation response does not match authoritative translation for paragraph $paragraph"
        )
      }
    }
  }
}

data class TranslationPair(
  val authoritativeTranslation: TranslateResponse?,
  val textToBeTranslated: ParagraphWithReference
)

@Serializable
data class TranslationModelResponse(
  val paragraphs: List<ParagraphWithReference>
)

@Serializable
data class TranslationResponse(
  val paragraphs: List<ParagraphWithAnnotation>
)
