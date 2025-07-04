package app.holybook.lib.translation.impl

import app.holybook.lib.translation.ModelConfiguration
import app.holybook.lib.translation.TextMatcher
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import javax.inject.Inject

class GeminiTextMatcher @Inject constructor(
    private val modelConfiguration: ModelConfiguration
) : TextMatcher {

    private val client = Client.builder().apiKey(modelConfiguration.apiKey).build()

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
        val response = client.models.generateContent(
            modelConfiguration.modelName,
            prompt,
            GenerateContentConfig.builder().build()
        )
        val responseText = response.text()
        return responseText ?: throw RuntimeException("Match failed")
    }
}
