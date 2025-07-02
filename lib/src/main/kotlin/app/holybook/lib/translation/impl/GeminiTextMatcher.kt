package app.holybook.lib.translation.impl

import app.holybook.lib.translation.TextMatcher
import com.google.genai.Client

class GeminiTextMatcher(
    private val apiKey: String,
    private val modelName: String
) : TextMatcher {

    private val client = Client.builder().apiKey(apiKey).build()

    override fun findBestMatch(
        sourceLanguage: String,
        targetLanguage: String,
        textInSourceLanguage: String,
        referenceInTargetLanguage: String
    ): String {
        val prompt = """
Given the following text in $sourceLanguage:
---
$textInSourceLanguage
---

And the following reference translation in $targetLanguage:
---
$referenceInTargetLanguage
---

Please identify and return the portion of the reference translation that is the most accurate translation of the source text. Only return the translated text, with no additional explanation or commentary.
        """.trimIndent()
        val response = client.models.generateContent(modelName, prompt)
        return response.text()
    }
}
