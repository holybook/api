package app.holybook

import app.holybook.db.DatabaseFactory
import app.holybook.plugins.configureRouting
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    System.err.println(environment.config.keys())
    DatabaseFactory.init(environment.config)
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    configureRouting()
}