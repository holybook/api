package app.holybook.lib.translation

import app.holybook.lib.models.TranslateResponse

data class ParagraphToBeTranslated(
  val translationId: String?,
  val text: String
)

data class TranslationTemplateData(
  val fromLanguage: String,
  val toLanguage: String,
  val authoritativeTranslations: List<TranslateResponse>,
  val paragraphsToBeTranslated: List<ParagraphToBeTranslated>
)
