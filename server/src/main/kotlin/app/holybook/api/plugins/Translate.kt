package app.holybook.api.plugins

import app.holybook.lib.translation.Translation
import app.holybook.lib.translation.TranslationModelResponse
import app.holybook.lib.translation.TranslationResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Routing.configureTranslate() {
  post("/api/aitranslate") {
    val request = call.receive<TranslateRequest>()
    val result = Translation.translate(
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

@Serializable
private data class TranslateResponse(
  val prompt: String
)
