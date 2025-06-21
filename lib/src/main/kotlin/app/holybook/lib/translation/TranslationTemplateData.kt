package app.holybook.lib.translation

import app.holybook.lib.models.TranslateResponse
import kotlinx.serialization.Serializable

@Serializable
data class ParagraphReference(
  val bookId: String,
  val index: Int
)

@Serializable
data class ParagraphWithReference(
  val reference: ParagraphReference? = null,
  val text: String
)

data class TranslationTemplateData(
  val fromLanguage: String,
  val toLanguage: String,
  val authoritativeTranslations: List<TranslateResponse>,
  val paragraphsToBeTranslated: List<ParagraphWithReference>
)
