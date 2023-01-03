package app.holybook.api.plugins

import app.holybook.api.models.Books
import app.holybook.api.models.Books.author
import app.holybook.api.models.Paragraphs
import app.holybook.api.models.Translations
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.configureBooks() {

  get("/books") {
    val books = transaction {
      Books.selectAll().map { Book(it[Books.id].value, it[author], null, listOf()) }
    }
    call.respond(books)
  }

  get("/books/{id}") {
    val id = call.parameters["id"]
    if (id == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }
    val bookRow = transaction { Books.select { Books.id eq id }.firstOrNull() }
    if (bookRow == null) {
      call.respond(HttpStatusCode.NotFound)
      return@get
    }

    val paragraphCount = transaction {
      Paragraphs.slice(Paragraphs.bookId.count(), Paragraphs.language)
          .select { Paragraphs.bookId eq id }
          .groupBy(Paragraphs.language)
          .firstOrNull()
          ?.get(Paragraphs.bookId.count())
          ?: 0
    }

    val translations = transaction {
      Translations.select { Translations.bookId eq id }
          .map { Translation(it[Translations.language], it[Translations.title]) }
    }
    call.respond(Book(id, bookRow[author], paragraphCount, translations))
  }
}

@Serializable
data class Book(
    val id: String,
    val author: String,
    val paragraphCount: Long?,
    val translations: List<Translation>
)

@Serializable data class Translation(val language: String, val title: String)
