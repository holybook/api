package app.holybook.api.plugins

import app.holybook.lib.models.getAllBooks
import app.holybook.lib.models.getBook
import app.holybook.lib.models.getSupportedLanguages
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.configureBooks() {

  get("/api/languages") {
    call.respond(getSupportedLanguages())
  }

  get("/api/books") {
    val language = call.parameters["lang"] ?: "en"
    call.respond(getAllBooks(language))
  }

  get("/api/books/{id}") {
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
