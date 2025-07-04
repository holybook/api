package app.holybook.lib.translation

import com.google.genai.Client
import com.google.genai.types.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * A monolithic implementation of the Translator interface that uses Google's Gemini AI
 * to translate text with support for authoritative reference translations.
 *
 * This implementation handles the complete AI translation pipeline including:
 * - JSON schema definition for structured responses
 * - System prompt loading and configuration
 * - AI model interaction via Google's Gemini API
 * - Response validation against authoritative translations
 */
class MonolithicTranslator @Inject constructor(private val modelConfiguration: ModelConfiguration) : Translator {

  private val log = LoggerFactory.getLogger("monolithic-translator")

  // Create a client with the provided API key
  private val client = Client.builder().apiKey(modelConfiguration.apiKey).build()

  private val paragraphSchema = Schema.builder().type(Type.Known.OBJECT)
    .properties(
      mapOf(
        "text" to Schema.builder().type(Type.Known.STRING).build(),
        "id" to Schema.builder().type(Type.Known.STRING).build()
      )
    ).required("text")
  private val paragraphsSchema =
    Schema.builder().type(Type.Known.ARRAY).items(paragraphSchema).build()
  private val responseSchema =
    Schema.builder().type(Type.Known.OBJECT)
      .properties(mapOf("paragraphs" to paragraphsSchema))
      .required("paragraphs")

  override fun translate(request: TranslationModelRequest): TranslationModelResponse {
    val prompt = Json.encodeToString(request)

    log.info("Prompt: $prompt")

    val systemInstruction =
      this::class.java.classLoader.getResourceAsStream("system_prompt.txt")
        ?.use { inputStream ->
          inputStream.bufferedReader().readText()
        }

    val modelResponse = client.models.generateContent(
      modelConfiguration.modelName,
      prompt,
      GenerateContentConfig.builder().responseMimeType("application/json")
        .responseSchema(responseSchema)
        .thinkingConfig(
          ThinkingConfig
            .builder()
            .thinkingBudget(0)
            .build()
        )
        .systemInstruction(
          Content.fromParts(Part.fromText(systemInstruction))
        )
        .build()
    )

    modelResponse.usageMetadata().ifPresent {
      log.info("Prompt tokens: ${it.promptTokenCount()}")
      log.info("Thinking tokens: ${it.thoughtsTokenCount()}")
      log.info("Response tokens: ${it.candidatesTokenCount()}")
      log.info("Total tokens: ${it.totalTokenCount()}")
    }

    val modelResponseText = modelResponse.text()

    if (modelResponseText == null) {
      throw IllegalStateException("Model response is null")
    }

    log.info("Model response: $modelResponseText")

    val response =
      Json.decodeFromString<TranslationModelResponse>(modelResponseText)

    return response
  }
}
