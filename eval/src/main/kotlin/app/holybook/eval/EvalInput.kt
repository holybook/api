package app.holybook.eval

import app.holybook.lib.translation.ParagraphWithId
import kotlinx.serialization.Serializable

@Serializable
data class EvalInput(
  val fromLanguage: String,
  val toLanguage: String,
  val paragraphs: List<InputParagraph>,
  val referenceParagraphs: List<ParagraphWithId> = emptyList(),
)

@Serializable data class InputParagraph(val text: String, val id: String? = null)
