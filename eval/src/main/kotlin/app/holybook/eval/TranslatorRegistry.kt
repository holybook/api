package app.holybook.eval

import app.holybook.lib.translation.IncrementalTranslator
import app.holybook.lib.translation.ParagraphTranslator
import app.holybook.lib.translation.TextMatcher
import app.holybook.lib.translation.Translator
import javax.inject.Inject

class TranslatorRegistry
@Inject
internal constructor(
  private val translators: @JvmSuppressWildcards Map<String, Translator>,
  private val paragraphTranslators: @JvmSuppressWildcards Map<String, ParagraphTranslator>,
  private val textMatchers: @JvmSuppressWildcards Map<String, TextMatcher>,
) {

  fun resolve(
    translatorName: String?,
    paragraphTranslatorName: String?,
    textMatcherName: String?,
  ): Translator {
    if (translatorName != null) {
      return translators[translatorName]
        ?: throw CommandLineFailed("Translator name not found: $translatorName")
    }

    if (paragraphTranslatorName == null || textMatcherName == null) {
      throw CommandLineFailed(
        "If no translator is provided, you must provide paragraph translator and text matcher."
      )
    }

    return IncrementalTranslator(
      paragraphTranslators[paragraphTranslatorName]
        ?: throw CommandLineFailed("Paragraph translator name not found: $paragraphTranslatorName"),
      textMatchers[textMatcherName]
        ?: throw CommandLineFailed("Text matcher name not found: $textMatcherName"),
    )
  }
}
