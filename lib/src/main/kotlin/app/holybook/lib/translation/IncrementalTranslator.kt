package app.holybook.lib.translation

import org.slf4j.LoggerFactory

/**
 * An incremental implementation of the Translator interface that translates paragraphs one by one
 * using separate services for translation and text matching.
 *
 * This implementation:
 * - Translates each paragraph individually
 * - Uses reference translations when available by finding the best matching substring
 * - Falls back to fresh translation when no reference is available or no good match is found
 * - Combines all translated paragraphs into a single response
 */
class IncrementalTranslator(
  private val paragraphTranslator: ParagraphTranslator,
  private val textMatcher: TextMatcher,
) : Translator {

  private val log = LoggerFactory.getLogger("incremental-translator")

  override fun translate(request: TranslationModelRequest): TranslationModelResponse {
    log.info(
      "Starting incremental translation from ${request.fromLanguage} to ${request.toLanguage} for ${request.paragraphs.size} paragraphs"
    )

    val translatedParagraphs =
      request.paragraphs.mapIndexed { index, paragraphWithReference ->
        log.debug("Translating paragraph ${index + 1}/${request.paragraphs.size}")

        val translatedText =
          when {
            // If we have a reference, try to find the best match
            paragraphWithReference.reference != null -> {
              val bestMatch =
                textMatcher.findBestMatch(
                  request.fromLanguage,
                  request.toLanguage,
                  paragraphWithReference.text,
                  paragraphWithReference.reference.text,
                )

              if (bestMatch.isNotBlank()) {
                log.debug("Found matching text in reference for paragraph ${index + 1}")
                bestMatch
              } else {
                log.debug(
                  "No good match found in reference, translating from scratch for paragraph ${index + 1}"
                )
                paragraphTranslator.translateParagraph(
                  request.fromLanguage,
                  request.toLanguage,
                  paragraphWithReference.text,
                )
              }
            }

            // No reference available, translate from scratch
            else -> {
              log.debug(
                "No reference available, translating from scratch for paragraph ${index + 1}"
              )
              paragraphTranslator.translateParagraph(
                request.fromLanguage,
                request.toLanguage,
                paragraphWithReference.text,
              )
            }
          }

        // Create the translated paragraph with ID if we used a reference
        ParagraphWithId(text = translatedText, id = paragraphWithReference.reference?.id)
      }

    log.info("Completed incremental translation of ${request.paragraphs.size} paragraphs")

    return TranslationModelResponse(paragraphs = translatedParagraphs)
  }
}
