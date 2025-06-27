package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.TranslateResponse
import app.holybook.lib.models.translate
import app.holybook.lib.translation.TranslateResponseExt.annotation
import app.holybook.lib.translation.TranslateResponseExt.id
import kotlinx.serialization.Serializable

/**
 * High-level translation service that orchestrates the translation process.
 *
 * This class handles the complete translation workflow including:
 * - Text preprocessing and paragraph splitting
 * - Authoritative translation lookup for reference
 * - AI translation request preparation
 * - Response post-processing and validation
 * - Final result formatting
 */
class Translation(private val translator: Translator) {

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
          textToBeTranslated = paragraphText
        )
      }

    val modelInput = TranslationModelRequest(
      fromLanguage,
      toLanguage,
      translationPairs.map { pair ->
        ParagraphWithReference(
          pair.textToBeTranslated,
          pair.authoritativeTranslation?.let {
            ParagraphWithId(
              text = it.translatedParagraph.text,
              id = it.id,
            )
          },
        )
      },
    )

    // Delegate to the injected translator
    val response = translator.translate(modelInput) ?: return null
    val authoritativeTranslationsMap = translationPairs.mapNotNull {
      if (it.authoritativeTranslation == null) {
        return@mapNotNull null
      }

      it.authoritativeTranslation.id to it.authoritativeTranslation
    }.toMap()

    // Validate the response against authoritative translations
    validateResponse(response, authoritativeTranslationsMap)

    return TranslationResponse(paragraphs = response.paragraphs.map { paragraph ->
      val authoritativeTranslation =
        paragraph.id?.let { authoritativeTranslationsMap[it] }
      ParagraphWithAnnotation(
        annotation = authoritativeTranslation?.annotation,
        text = paragraph.text
      )
    })
  }

  /**
   * Validates that the translation response matches authoritative translations where available.
   * This is a generic validation that works with any TranslationModelResponse regardless of
   * the Translator implementation used.
   *
   * @param response The translation response to validate
   * @param authoritativeTranslations Map of reference IDs to authoritative translations
   * @throws IllegalStateException if validation fails
   */
  private fun validateResponse(
    response: TranslationModelResponse,
    authoritativeTranslations: Map<String, TranslateResponse>
  ) {
    for (paragraph in response.paragraphs) {
      val reference = paragraph.id ?: continue
      val authoritativeText =
        authoritativeTranslations[reference]?.translatedParagraph?.text
          ?: continue
      if (!authoritativeText.contains(paragraph.text, ignoreCase = true)) {
        throw IllegalStateException(
          "Translation response does not match authoritative translation for paragraph $paragraph"
        )
      }
    }
  }
}

data class TranslationPair(
  val authoritativeTranslation: TranslateResponse?,
  val textToBeTranslated: String
)



@Serializable
data class TranslationResponse(
  val paragraphs: List<ParagraphWithAnnotation>
)
