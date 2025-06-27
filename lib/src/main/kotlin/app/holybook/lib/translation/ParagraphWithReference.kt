package app.holybook.lib.translation

import kotlinx.serialization.Serializable

/**
 * Represents a paragraph that may include a reference to another paragraph.
 * 
 * Used in translation requests where a paragraph to be translated can optionally
 * reference an authoritative translation for context and validation.
 */
@Serializable
data class ParagraphWithReference(
  val text: String,
  val reference: ParagraphWithId? = null,
)
