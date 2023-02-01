package app.holybook.lib.models

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.map
import java.sql.Connection
import java.sql.Date
import java.time.LocalDate
import kotlinx.serialization.Serializable

fun Connection.createBooksTable() {
  createStatement().executeUpdate("""
    CREATE TABLE IF NOT EXISTS books (
        id VARCHAR(32) NOT NULL,
        author SERIAL NOT NULL REFERENCES authors,
        published_at DATE NULL,

        PRIMARY KEY (id)
    );
  """.trimIndent())
}

fun Connection.dropBooksTable() {
  createStatement().executeUpdate("""
    DROP TABLE IF EXISTS books;
  """.trimIndent())
}

fun getAllBooks() = transaction {
  prepareStatement("""
    SELECT books.id, author_names.name, translations.language, translations.title 
    FROM translations 
    INNER JOIN books ON 
        translations.book = books.id
    INNER JOIN author_names ON 
        author_names.language = translations.language AND
        author_names.id = books.author
  """.trimIndent()).executeQuery().map {
    Pair(Pair(getString("id"), getString("name")),
         Translation(getString("language"), getString("title"))
    )
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

fun Connection.insertBook(id: String, authorId: Int, publishedAt: LocalDate?) {
  val preparedStatement = prepareStatement("""
      INSERT INTO books(id, author, published_at) VALUES (?, ?, ?) ON CONFLICT DO NOTHING
    """.trimIndent())

  preparedStatement.setString(1, id)
  preparedStatement.setInt(2, authorId)
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