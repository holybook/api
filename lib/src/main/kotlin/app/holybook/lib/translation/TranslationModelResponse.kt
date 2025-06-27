package app.holybook.lib.translation

import kotlinx.serialization.Serializable

/**
 * Response model from AI translation services.
 * 
 * Contains the translated paragraphs with optional reference IDs.
 */
@Serializable
data class TranslationModelResponse(
  val paragraphs: List<ParagraphWithId>
)
