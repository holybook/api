package app.holybook.api.plugins

import app.holybook.lib.models.getAllAuthors
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.configureAuthors() {

  get("/api/authors") { call.respond(getAllAuthors()) }
}
