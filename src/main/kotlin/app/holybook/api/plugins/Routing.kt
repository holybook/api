package app.holybook.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  routing {
    singlePageApplication {
      react("static")
    }
    configureBooks()
    configureParagraphs()
    configureSearch()
  }
}