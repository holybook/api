package app.holybook.api

import app.holybook.api.config.ApplicationConfigExt.getDbPassword
import app.holybook.api.config.ApplicationConfigExt.getDbUser
import app.holybook.api.config.ApplicationConfigExt.getJdbcUrl
import app.holybook.api.plugins.configureRouting
import app.holybook.lib.db.Database
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
  Database.init(
    environment.config.getJdbcUrl(),
    environment.config.getDbUser(),
    environment.config.getDbPassword(),
  )

  val isDevelopment = developmentMode

  install(ContentNegotiation) { json() }
  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }
  install(CORS) {
    // The dev frontend (npm start) runs cross-origin on :3000; in production the
    // frontend is served same-origin by Caddy, so this allowance is dev-only.
    if (isDevelopment) {
      allowHost("localhost:3000")
    }
    allowHost("holybook.app", schemes = listOf("http", "https"))
    allowHeader(HttpHeaders.ContentType)
  }
  configureRouting()
}
