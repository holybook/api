package app.holybook.api

import app.holybook.lib.db.Database
import app.holybook.api.plugins.configureRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
  Database.init(environment.config.getJdbcUrl())
  install(ContentNegotiation) {
    json()
  }
  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }
  install(CORS) {
    allowHost("localhost:3000")
    allowHost("holybook.app", schemes = listOf("http", "https"))
    allowHeader(HttpHeaders.ContentType)
  }
  configureRouting()
}

fun ApplicationConfig.getJdbcUrl(): String {
  val host = property("storage.hostName").getString()
  val port = property("storage.port").getString()
  val db = property("storage.dbName").getString()
  val user = property("storage.userName").getString()
  val passwordParameter = propertyOrNull("storage.password")?.getString().let {
    if (it.isNullOrEmpty()) {
      null
    } else {
      "&password=$it"
    }
  } ?: ""
  return "jdbc:postgresql://$host:$port/$db?user=$user$passwordParameter"
}