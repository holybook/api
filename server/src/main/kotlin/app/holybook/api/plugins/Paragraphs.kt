package app.holybook.api.plugins

import app.holybook.lib.models.getParagraphs
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Routing.configureParagraphs() {
  get("/api/books/{id}/paragraphs") { fetchParagraphs() }
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchParagraphs() {
  val bookId = call.parameters["id"]

  if (bookId == null) {
    call.respond(HttpStatusCode.NotFound)
    return
  }

  val language = call.request.queryParameters["lang"] ?: "en"
  val startIndex = call.request.queryParameters["start"]?.toInt()
  val endIndex = call.request.queryParameters["end"]?.toInt()

  val paragraphs = getParagraphs(bookId, language, startIndex, endIndex)
  call.respond(paragraphs)
}
