package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.translate

object Translation {

    fun translate(fromLanguage: String, toLanguage: String, text: String): String {
        val paragraphs = text.split(" ")

        val translatedParagraphs = mutableListOf<String>()
        val annotatedParagraphs = mutableListOf<String>()
        var translationIdCounter = 0

        paragraphs.forEach { paragraphText ->
            val result = translate(
                TranslateRequest(
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    text = paragraphText
                )
            )

            if (result != null) {
                val translationId = translationIdCounter++
                translatedParagraphs.add("<translation translationId=\"$translationId\">${result.translatedParagraph.text}</translation>")
                annotatedParagraphs.add("<p translationId=\"$translationId\">$paragraphText</p>")
            } else {
                annotatedParagraphs.add("<p>$paragraphText</p>")
            }
        }

        val translatedBlock = translatedParagraphs.joinToString("\n\n")
        val originalBlock = annotatedParagraphs.joinToString("\n\n")

        val geminiPrompt = """
Translate the paragraphs inside the <p> elements. Some paragraphs contain holy writings. If they do so the <p> element has an attribute `translationId`. For these paragraphs DO NOT translate it by yourself. Look up the correct translation from the corresponding <translation> elements with the same `translationId`. Note that they might contain more text than in the original language. In this case, please only take the substring that corresponds to the original but DO NOT modify any text. Only take verbatim substrings.

$translatedBlock

$originalBlock
"""
        return geminiPrompt
    }
}
