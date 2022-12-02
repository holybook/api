import db.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import plugins.configureRouting

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::holyBook
    ).start(wait = true)
}

fun Application.holyBook() {
    DatabaseFactory.init(environment.config)
    configureRouting()
}