package app.holybook.api.plugins

import app.holybook.api.config.ApplicationConfigExt.getGeminiApiKey
import app.holybook.lib.translation.Translation
import app.holybook.lib.translation.TranslationResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Routing.configureTranslate() {

  val translation by lazy {
    Translation(environment.config.getGeminiApiKey())
  }

  post("/api/aitranslate") {
    val request = call.receive<TranslateRequest>()
    val result = translation.translate(
      request.fromLanguage,
      request.toLanguage,
      request.text
    )

    if (result == null) {
      call.respond(HttpStatusCode.NotFound)
      return@post
    }

    call.respond<TranslationResponse>(result)
  }
}

@Serializable
private data class TranslateRequest(
  val fromLanguage: String,
  val toLanguage: String,
  val text: String
)
