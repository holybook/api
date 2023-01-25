package app.holybook.api.models

import app.holybook.api.db.Database.transaction
import app.holybook.api.db.map
import java.sql.Connection
import java.sql.Date
import java.time.LocalDate
import kotlinx.serialization.Serializable

fun Connection.createBooksTable() {
  createStatement().executeUpdate("""
    CREATE TABLE IF NOT EXISTS books (
        id VARCHAR(32) NOT NULL,
        author VARCHAR(256) NOT NULL,
        published_at DATE NULL,

        PRIMARY KEY (id)
    );
  """.trimIndent())
}

fun Connection.dropBooksTable() {
  createStatement().executeUpdate("""
    DROP TABLE books;
  """.trimIndent())
}

fun getAllBooks() = transaction {
  prepareStatement("""
    SELECT id, author, language, title 
    FROM translations INNER JOIN books ON translations.book = books.id
  """.trimIndent()).executeQuery().map {
    Pair(Pair(getString("id"), getString("author")),
         Translation(getString("language"), getString("title")))
  }.groupBy {
    it.first
  }.entries.map {
    Book(it.key.first, it.key.second, null, it.value.map {
      it.second
    })
  }
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

fun Connection.insertBook(id: String, author: String, publishedAt: LocalDate?) {
  val preparedStatement = prepareStatement("""
      INSERT INTO books(id, author, published_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING
    """.trimIndent())

  preparedStatement.setString(1, id)
  preparedStatement.setString(2, author)
  preparedStatement.setDate(3, publishedAt?.let(Date::valueOf))
  preparedStatement.executeUpdate()
}

@Serializable
data class Book(
  val id: String,
  val author: String,
  val paragraphCount: Long?,
  val translations: List<Translation>,
)