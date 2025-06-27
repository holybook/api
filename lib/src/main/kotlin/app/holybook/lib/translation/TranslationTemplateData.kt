package app.holybook.lib.translation

import app.holybook.lib.models.TranslateResponse
import kotlinx.serialization.Serializable

@Serializable
data class ParagraphAnnotation(
  val bookId: String,
  val index: Int,
  val number: Int? = null,
  val title: String,
)

@Serializable
data class ParagraphWithAnnotation(
  val annotation: ParagraphAnnotation? = null,
  val text: String
)

@Serializable
data class ParagraphReference(
  val bookId: String,
  val index: Int
)

data class TranslationTemplateData(
  val fromLanguage: String,
  val toLanguage: String,
  val authoritativeTranslations: List<TranslateResponse>,
  val paragraphsToBeTranslated: List<ParagraphWithId>
)