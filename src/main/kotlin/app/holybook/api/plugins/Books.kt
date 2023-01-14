package app.holybook.api.plugins

import app.holybook.api.models.getAllBooks
import app.holybook.api.models.getBook
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.configureBooks() {

  get("/api/books") {
    call.respond(getAllBooks())
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
