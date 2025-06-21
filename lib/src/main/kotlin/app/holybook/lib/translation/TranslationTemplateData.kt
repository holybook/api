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

data class TranslationTemplateData(
  val fromLanguage: String,
  val toLanguage: String,
  val authoritativeTranslations: List<TranslateResponse>,
  val paragraphsToBeTranslated: List<ParagraphWithReference>
)

val TranslateResponse.bookId: String
  get() = allOriginalResults[0].bookId

val TranslateResponse.reference: ParagraphReference
  get() = ParagraphReference(bookId, translatedParagraph.index)

val TranslateResponse.annotation: ParagraphAnnotation
  get() = ParagraphAnnotation(
    bookId,
    index = translatedParagraph.index,
    number = translatedParagraph.number,
    title = allOriginalResults[0].title
  )