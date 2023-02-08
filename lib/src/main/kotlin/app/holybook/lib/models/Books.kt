package app.holybook.lib.models

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.map
import java.sql.Connection
import java.sql.Date
import java.time.LocalDate
import kotlinx.serialization.Serializable

fun Connection.createBooksTable() {
  createStatement()
    .executeUpdate(
      """
    CREATE TABLE IF NOT EXISTS books (
        id VARCHAR(32) NOT NULL,
        author VARCHAR(32) NOT NULL,
        published_at DATE NULL,

        PRIMARY KEY (id)
    );
  """
        .trimIndent()
    )
}

fun Connection.dropBooksTable() {
  createStatement()
    .executeUpdate(
      """
    DROP TABLE IF EXISTS books;
  """
        .trimIndent()
    )
}

fun getAllBooks(language: String) = transaction {
  val getBooks =
    prepareStatement(
      """
    SELECT books.id, books.author, translations.language, translations.title 
    FROM translations 
    INNER JOIN books ON translations.book = books.id
    WHERE translations.language = ?
    ORDER BY books.id DESC
  """
        .trimIndent()
    )
  getBooks.setString(1, language)
  getBooks
    .executeQuery()
    .map {
      Translation(
        bookId = getString("id"),
        language = language,
        title = getString("title"),
        author = getAuthorName(getString("author"), language)
      )
    }
    .groupBy { it.author }
}

fun getBook(id: String): Book? = transaction {
  val getBook =
    prepareStatement(
      """
        SELECT id, author FROM books WHERE id = ?
      """
        .trimIndent()
    )
  getBook.setString(1, id)
  val bookRow = getBook.executeQuery()
  if (!bookRow.next()) {
    return@transaction null
  }
  val authorCode = bookRow.getString("author")

  val paragraphCount = getParagraphCount(id)
  val translations = getTranslations(id, authorCode)
  Book(id, paragraphCount, translations)
}

private fun Connection.getParagraphCount(id: String): Long {
  val getParagraphCount =
    prepareStatement(
      """
        SELECT COUNT(*) FROM paragraphs WHERE book = ? GROUP BY language
      """
        .trimIndent()
    )
  getParagraphCount.setString(1, id)
  val paragraphCountRows = getParagraphCount.executeQuery()
  return if (paragraphCountRows.next()) {
    paragraphCountRows.getLong(1)
  } else {
    0
  }
}

fun Connection.insertBook(id: String, author: String, publishedAt: LocalDate?) {
  val preparedStatement =
    prepareStatement(
      """
      INSERT INTO books(id, author, published_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING
    """
        .trimIndent()
    )

  preparedStatement.setString(1, id)
  preparedStatement.setString(2, author)
  preparedStatement.setDate(3, publishedAt?.let(Date::valueOf))
  preparedStatement.executeUpdate()
}

class BookRow(val id: String, val author: String)

@Serializable
data class Book(
  val id: String,
  val paragraphCount: Long?,
  val translations: List<Translation>,
)
