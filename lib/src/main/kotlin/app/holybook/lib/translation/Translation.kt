package app.holybook.lib.translation

import app.holybook.lib.models.TranslateRequest
import app.holybook.lib.models.TranslateResponse
import app.holybook.lib.models.translate
import app.holybook.lib.translation.TranslateResponseExt.annotation
import app.holybook.lib.translation.TranslateResponseExt.bookId
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Schema
import com.google.genai.types.Type
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class Translation(private val apiKey: String) {

  private val log = LoggerFactory.getLogger("translation")

  // Create a client with the provided API key
  private val client = Client.builder().apiKey(apiKey).build()

  private val referenceSchema =
    Schema.builder().type(Type.Known.OBJECT).properties(
      mapOf(
        "bookId" to Schema.builder().type(Type.Known.STRING).build(),
        "index" to Schema.builder().type(Type.Known.INTEGER).build()
      )
    ).build()
  private val paragraphSchema = Schema.builder().type(Type.Known.OBJECT)
    .properties(
      mapOf(
        "text" to Schema.builder().type(Type.Known.STRING).build(),
        "reference" to referenceSchema
      )
    )
  private val paragraphsSchema =
    Schema.builder().type(Type.Known.ARRAY).items(paragraphSchema).build()
  private val responseSchema =
    Schema.builder().type(Type.Known.OBJECT)
      .properties(mapOf("paragraphs" to paragraphsSchema))

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
          textToBeTranslated = ParagraphWithReference(
            translateResponse?.let {
              ParagraphReference(
                it.bookId,
                it.translatedParagraph.index
              )
            },
            paragraphText
          ),
        )
      }

    val prompt = TranslationTemplateEngine.renderPrompt(
      TranslationTemplateData(
        fromLanguage,
        toLanguage,
        translationPairs.mapNotNull { it.authoritativeTranslation },
        translationPairs.map { it.textToBeTranslated }
      )
    )

    log.info("Prompt: $prompt")

    val modelResponse = client.models.generateContent(
      "gemini-2.5-flash",
      prompt,
      GenerateContentConfig.builder().responseMimeType("application/json")
        .responseSchema(responseSchema)
        .build()
    )

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

      it.textToBeTranslated.reference!! to it.authoritativeTranslation
    }.toMap()

    response.validate(authoritativeTranslationsMap)

    return TranslationResponse(paragraphs = response.paragraphs.map { paragraph ->
      val authoritativeTranslation =
        paragraph.reference?.let { authoritativeTranslationsMap[it] }
      ParagraphWithAnnotation(
        annotation = authoritativeTranslation?.annotation,
        text = paragraph.text
      )
    })
  }

  private fun TranslationModelResponse.validate(authoritativeTranslations: Map<ParagraphReference, TranslateResponse>) {
    for (paragraph in paragraphs) {
      val reference = paragraph.reference ?: continue
      val authoritativeText =
        authoritativeTranslations[reference]?.translatedParagraph?.text
          ?: continue
      if (!authoritativeText.contains(paragraph.text)) {
        throw IllegalStateException(
          "Translation response does not match authoritative translation for paragraph $paragraph"
        )
      }
    }
  }
}

data class TranslationPair(
  val authoritativeTranslation: TranslateResponse?,
  val textToBeTranslated: ParagraphWithReference
)

@Serializable
data class TranslationModelResponse(
  val paragraphs: List<ParagraphWithReference>
)

@Serializable
data class TranslationResponse(
  val paragraphs: List<ParagraphWithAnnotation>
)
