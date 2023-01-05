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
    val query = call.parameters["query"]
    if (query == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }

    call.respond(searchParagraphs(language, query))
  }

  get("/books/{id}") {
    val id = call.parameters["id"]
    if (id == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }

    val book = getBook(id)
    if (book == null) {
      call.respond(HttpStatusCode.NotFound)
      return@get
    }

    call.respond(book)
  }
}
