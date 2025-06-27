package app.holybook.lib.translation

import app.holybook.lib.models.TranslateResponse

object TranslateResponseExt {
  val TranslateResponse.bookId: String
    get() = allOriginalResults[0].bookId

  val TranslateResponse.id: String
    get() = "$bookId/${translatedParagraph.index}"

  val TranslateResponse.annotation: ParagraphAnnotation
    get() = ParagraphAnnotation(
      bookId,
      index = translatedParagraph.index,
      number = translatedParagraph.number,
      title = allOriginalResults[0].title
    )
}