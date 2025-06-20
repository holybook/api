package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.translate

object Translation {

  fun translate(
    fromLanguage: String,
    toLanguage: String,
    text: String
  ): String {
    val paragraphs = text.lines()

    val translateResponses =
      paragraphs.mapNotNull { paragraphText ->
        translate(
          TranslateRequest(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            text = paragraphText,
          )
        )
      }

    return TranslationTemplateEngine.renderPrompt(
      TranslationTemplateData(
        translateResponses
      )
    )
  }
}
