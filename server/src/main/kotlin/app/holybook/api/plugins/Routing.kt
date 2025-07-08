package app.holybook.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  routing {
    configureAuthors()
    configureBooks()
    configureParagraphs()
    configureSearch()
    configureTranslate()
  }
}
