package app.holybook.api.plugins

import app.holybook.api.config.ApplicationConfigExt.getGeminiApiKey
import app.holybook.api.modules.DaggerServerComponent
import app.holybook.lib.translation.ModelConfiguration
import app.holybook.lib.translation.Translation
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Routing.configureTranslate() {
  val component =
    DaggerServerComponent.builder()
      .modelConfiguration(
        ModelConfiguration(
          apiKey = environment.config.getGeminiApiKey(),
          modelName = "gemini-2.5-flash-lite-preview-06-17",
        )
      )
      .build()
  val translator = component.translator()
  val translation by lazy { Translation(translator) }

  post("/api/aitranslate") {
    val request = call.receive<TranslateRequest>()
    val result = translation.translate(request.fromLanguage, request.toLanguage, request.text)

    if (result == null) {
      call.respond(HttpStatusCode.NotFound)
      return@post
    }

    call.respond(result)
  }
}

@Serializable
private data class TranslateRequest(
  val fromLanguage: String,
  val toLanguage: String,
  val text: String,
)
