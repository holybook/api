package app.holybook.api

import app.holybook.api.db.Database
import app.holybook.api.plugins.configureRouting
import app.holybook.import.fetchAndImportIndex
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import kotlinx.coroutines.launch
import org.slf4j.event.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    System.err.println(environment.config.keys())
    Database.init(environment.config, log)
    launch {
        fetchAndImportIndex(log)
    }
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CORS) {
        allowHost("localhost:3000")
        allowHeader(HttpHeaders.ContentType)
    }
    configureRouting()
}