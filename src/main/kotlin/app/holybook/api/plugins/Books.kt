package app.holybook.api.plugins

import app.holybook.api.db.Database
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
    val query = Database.getConnection().prepareStatement("SELECT id, author FROM books")
    val result = query.executeQuery()
    val books = mutableListOf<Book>()

    while (result.next()) {
      books.add(Book(id = result.getString("id"),
                     author = result.getString("author"),
                     paragraphCount = null,
                     translations = listOf()))
    }

    call.respond(books)
  }

   get("/books/{id}") {
     val id = call.parameters["id"]
     if (id == null) {
       call.respond(HttpStatusCode.BadRequest)
       return@get
     }

     val getBook = Database.getConnection().prepareStatement("""
       SELECT id, author FROM books WHERE id = ?
     """.trimIndent())
     getBook.setString(1, id)
     val bookRow = getBook.executeQuery()
     if (!bookRow.next()) {
       call.respond(HttpStatusCode.NotFound)
       return@get
     }
     val author = bookRow.getString("author")

     val getParagraphCount = Database.getConnection().prepareStatement("""
         SELECT COUNT(*) FROM paragraphs WHERE book = ? GROUP BY language
     """.trimIndent())
     getParagraphCount.setString(1, id)
     val paragraphCountRows = getParagraphCount.executeQuery()
     val paragraphCount = if (paragraphCountRows.next()) {
         paragraphCountRows.getLong(1)
     } else {
         0
     }

     val getTranslations = Database.getConnection().prepareStatement("""
         SELECT language, title FROM translations WHERE book = ?
     """.trimIndent())
     getTranslations.setString(1, id)
     val translationRows = getTranslations.executeQuery()
     val translations = mutableListOf<Translation>()
     while (translationRows.next()) {
         translations.add(
             Translation(
                 translationRows.getString("language"),
                 translationRows.getString("title")
             )
         )
     }

     call.respond(Book(id, author, paragraphCount, translations))
   }
}

@Serializable
data class Book(
  val id: String,
  val author: String,
  val paragraphCount: Long?,
  val translations: List<Translation>,
)

@Serializable
data class Translation(val language: String, val title: String)
