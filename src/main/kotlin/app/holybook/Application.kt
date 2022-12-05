package app.holybook

import app.holybook.db.DatabaseFactory
import app.holybook.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    System.err.println(environment.config.keys())
    DatabaseFactory.init(environment.config)
    configureRouting()
}