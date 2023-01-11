package app.holybook.api.plugins

import app.holybook.api.models.getBook
import app.holybook.api.models.searchParagraphs
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.configureSearch() {

  get("/search") {
    val language = call.parameters["lang"] ?: "en"
    val query = call.parameters["q"]
    if (query == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }

    call.respond(searchParagraphs(language, query))
  }
}
