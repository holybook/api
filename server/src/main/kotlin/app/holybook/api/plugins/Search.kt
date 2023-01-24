package app.holybook.api.plugins

import app.holybook.api.models.TranslateRequest
import app.holybook.api.models.getBook
import app.holybook.api.models.searchParagraphs
import app.holybook.api.models.translate
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Routing.configureSearch() {

  get("/api/search") {
    val language = call.parameters["lang"] ?: "en"
    val query = call.parameters["q"]
    if (query == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }

    call.respond(searchParagraphs(language, query))
  }

  post("/api/translate") {
    val request = call.receive<TranslateRequest>()
    val response = translate(request)
    if (response == null) {
      call.respond(HttpStatusCode.NotFound)
      return@post
    }

    call.respond(response)
  }
}
