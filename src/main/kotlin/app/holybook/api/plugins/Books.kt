package app.holybook.api.plugins

import app.holybook.api.models.Books
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.configureBooks() {
//    post("/books") {
//        val request = call.receive<CreateBookRequest>()
//        val id = transaction {
//            Books.insert {
//                it[title] = request.title
//            } get Books.id
//        }
//        call.respond(CreateBookResponse(id.value))
//    }

    get("/books") {
        val books = transaction {
            Books.selectAll().map {
                Book(it[Books.id].value, it[Books.author])
            }
        }
        call.respond(books)
    }

    get("/books/{id}") {
        val id = call.parameters["id"]?.toInt()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        val book = transaction {
            Books.select { Books.id eq id }.map {
                Book(it[Books.id].value, it[Books.author])
            }.firstOrNull()
        }
        if (book == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        call.respond(book)
    }

//    delete("/books/{id}") {
//        val id = call.parameters["id"]?.toInt()
//        if (id == null) {
//            call.respond(HttpStatusCode.BadRequest)
//            return@delete
//        }
//        val numItemsDeleted = transaction {
//            Books.deleteWhere { Books.id eq id }
//        }
//        call.respond(DeleteBookResponse(numItemsDeleted))
//    }
}

//@Serializable
//data class CreateBookRequest(val title: String)
//
//@Serializable
//data class CreateBookResponse(val id: Int)
//
//@Serializable
//data class DeleteBookResponse(val numItemsDeleted: Int)

@Serializable
data class Book(val id: Int, val author: String)