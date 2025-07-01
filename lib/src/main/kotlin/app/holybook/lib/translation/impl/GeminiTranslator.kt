package app.holybook.lib.translation.impl

import app.holybook.lib.translation.ParagraphTranslator
import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerateContentResponse
import com.google.cloud.vertexai.generativeai.GenerativeModel

class GeminiTranslator(
    private val project: String,
    private val location: String,
    private val modelName: String = "gemini-1.5-flash-001"
) : ParagraphTranslator {

    private val model: GenerativeModel

    init {
        val vertexAi = VertexAI(project, location)
        model = GenerativeModel(modelName, vertexAi)
    }
    
    override fun translateParagraph(
        fromLanguage: String,
        toLanguage: String,
        paragraphText: String
    ): String {
        val prompt = "Translate the following text from $fromLanguage to $toLanguage: $paragraphText"
        val response: GenerateContentResponse = model.generateContent(prompt)
        return response.candidatesList.first().content.partsList.first().text
    }
}
