package app.holybook.lib.translation

import kotlinx.serialization.Serializable

/**
 * Represents a paragraph with an optional identifier.
 *
 * Used in translation responses to identify paragraphs that can be referenced by other translation
 * requests or for validation purposes.
 */
@Serializable data class ParagraphWithId(val text: String, val id: String? = null)
