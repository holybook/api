package app.holybook.lib.translation.impl

import app.holybook.lib.translation.ParagraphTranslator
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig

class GeminiTranslator(
    private val apiKey: String,
    private val modelName: String
) : ParagraphTranslator {

    private val client = Client.builder().apiKey(apiKey).build()

    override fun translateParagraph(
        fromLanguage: String,
        toLanguage: String,
        paragraphText: String
    ): String {
        val prompt = "Translate the following text from $fromLanguage to $toLanguage: $paragraphText"
        val response = client.models.generateContent(
            modelName,
            prompt,
            GenerateContentConfig.builder().build()
        )
        val responseText = response.text()
        return responseText ?: throw RuntimeException("Translation failed")
    }
}
