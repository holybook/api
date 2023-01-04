package app.holybook.api.models

import app.holybook.api.db.Database.transaction
import java.sql.Connection
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

fun getAllBooks() = transaction {
  val result = prepareStatement("SELECT id, author FROM books").executeQuery()
  val books = mutableListOf<Book>()
  while (result.next()) {
    books.add(Book(id = result.getString("id"),
                   author = result.getString("author"),
                   paragraphCount = null,
                   translations = listOf()))
  }
  books
}

fun getBook(id: String): Book? = transaction {
  val getBook = prepareStatement("""
        SELECT id, author FROM books WHERE id = ?
      """.trimIndent())
  getBook.setString(1, id)
  val bookRow = getBook.executeQuery()
  if (!bookRow.next()) {
    return@transaction null
  }
  val author = bookRow.getString("author")

  val paragraphCount = getParagraphCount(id)
  val translations = getTranslations(id)
  Book(id, author, paragraphCount, translations)
}

private fun Connection.getParagraphCount(id: String): Long {
  val getParagraphCount = prepareStatement("""
        SELECT COUNT(*) FROM paragraphs WHERE book = ? GROUP BY language
      """.trimIndent())
  getParagraphCount.setString(1, id)
  val paragraphCountRows = getParagraphCount.executeQuery()
  return if (paragraphCountRows.next()) {
    paragraphCountRows.getLong(1)
  } else {
    0
  }
}

fun Connection.insertBook(id: String, author: String) {
  val preparedStatement = prepareStatement("""
      INSERT INTO books(id, author) VALUES (?, ?) ON CONFLICT DO NOTHING
    """.trimIndent())

  preparedStatement.setString(1, id)
  preparedStatement.setString(2, author)
  preparedStatement.executeUpdate()
}

@Serializable
data class Book(
  val id: String,
  val author: String,
  val paragraphCount: Long?,
  val translations: List<Translation>,
)