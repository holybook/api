package app.holybook.plugins

import app.holybook.models.Books
import kotlinx.serialization.Serializable
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.configureBooks() {
    post("/book") {
        val request = call.receive<CreateBookRequest>()
        val id = transaction {
            Books.insert {
                it[title] = request.title
            } get Books.id
        }
        call.respond(CreateBookResponse(id.value))
    }
}

@Serializable
data class CreateBookRequest(val title: String)

@Serializable
data class CreateBookResponse(val id: Int)