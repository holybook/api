package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.TranslateResponse
import app.holybook.lib.models.translate
import app.holybook.lib.translation.TranslateResponseExt.annotation
import app.holybook.lib.translation.TranslateResponseExt.id
import com.google.genai.Client
import com.google.genai.types.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class Translation(private val apiKey: String) {

  private val log = LoggerFactory.getLogger("translation")

  // Create a client with the provided API key
  private val client = Client.builder().apiKey(apiKey).build()

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
      .properties(mapOf("paragraphs" to paragraphsSchema)).required("paragraphs")

  fun translate(
    fromLanguage: String,
    toLanguage: String,
    text: String
  ): TranslationResponse? {
    val paragraphs =
      text.lines().mapNotNull { it.trim().takeIf { it.isNotBlank() } }

    val translationPairs =
      paragraphs.map { paragraphText ->
        val translateResponse = translate(
          TranslateRequest(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            text = paragraphText,
          )
        )
        TranslationPair(
          authoritativeTranslation = translateResponse,
          textToBeTranslated = paragraphText
        )
      }

    val modelInput = TranslationModelRequest(
      fromLanguage,
      toLanguage,
      translationPairs.map { pair ->
        ParagraphWithReference(
          pair.textToBeTranslated,
          pair.authoritativeTranslation?.let {
            ParagraphWithId(
              text = it.translatedParagraph.text,
              id = it.id,
            )
          },
        )
      },
    )
    val prompt = Json.encodeToString(modelInput)

    log.info("Prompt: $prompt")

    val systemInstruction =
      this::class.java.classLoader.getResourceAsStream("system_prompt.txt")
        ?.use { inputStream ->
          inputStream.bufferedReader().readText()
        }

    val modelResponse = client.models.generateContent(
      "gemini-2.5-flash-lite-preview-06-17",
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
      return null
    }

    log.info("Model response: $modelResponseText")

    val response =
      Json.decodeFromString<TranslationModelResponse>(modelResponseText)
    val authoritativeTranslationsMap = translationPairs.mapNotNull {
      if (it.authoritativeTranslation == null) {
        return@mapNotNull null
      }

      it.authoritativeTranslation.id to it.authoritativeTranslation
    }.toMap()

    response.validate(authoritativeTranslationsMap)

    return TranslationResponse(paragraphs = response.paragraphs.map { paragraph ->
      val authoritativeTranslation =
        paragraph.id?.let { authoritativeTranslationsMap[it] }
      ParagraphWithAnnotation(
        annotation = authoritativeTranslation?.annotation,
        text = paragraph.text
      )
    })
  }

  private fun TranslationModelResponse.validate(authoritativeTranslations: Map<String, TranslateResponse>) {
    for (paragraph in paragraphs) {
      val reference = paragraph.id ?: continue
      val authoritativeText =
        authoritativeTranslations[reference]?.translatedParagraph?.text
          ?: continue
      if (!authoritativeText.contains(paragraph.text, ignoreCase = true)) {
        throw IllegalStateException(
          "Translation response does not match authoritative translation for paragraph $paragraph"
        )
      }
    }
  }
}

data class TranslationPair(
  val authoritativeTranslation: TranslateResponse?,
  val textToBeTranslated: String
)

@Serializable
data class TranslationModelRequest(
  val fromLanguage: String,
  val toLanguage: String,
  val paragraphs: List<ParagraphWithReference>
)

@Serializable
data class TranslationModelResponse(
  val paragraphs: List<ParagraphWithId>
)

@Serializable
data class TranslationResponse(
  val paragraphs: List<ParagraphWithAnnotation>
)
