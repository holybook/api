package app.holybook.lib.translation

import kotlinx.serialization.Serializable

/**
 * Request model for AI translation services.
 *
 * Contains the source and target languages along with paragraphs to be translated. Each paragraph
 * may include a reference translation for context.
 */
@Serializable
data class TranslationModelRequest(
  val fromLanguage: String,
  val toLanguage: String,
  val paragraphs: List<ParagraphWithReference>,
)
