package app.holybook.lib.models

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.map
import java.sql.Connection
import kotlinx.serialization.Serializable

fun Connection.createTranslationsTable() {
  createStatement()
    .executeUpdate(
      """
    CREATE TABLE IF NOT EXISTS translations (
        book VARCHAR(32) NOT NULL,
        language VARCHAR(3) NOT NULL,
        title VARCHAR(512) NOT NULL,
    
        PRIMARY KEY (book, language),
        FOREIGN KEY (book) REFERENCES books(id)
    )
  """
        .trimIndent()
    )
}

fun Connection.dropTranslationsTable() {
  createStatement()
    .executeUpdate(
      """
    DROP TABLE IF EXISTS translations;
  """
        .trimIndent()
    )
}

fun getSupportedLanguages() = transaction {
  prepareStatement(
      """
        SELECT DISTINCT language from translations
    """
        .trimIndent()
    )
    .executeQuery()
    .map { getString("language") }
}

fun Connection.getTranslations(bookId: String, authorCode: String): List<Translation> {
  val getTranslations =
    prepareStatement(
      """
        SELECT language, title FROM translations WHERE book = ?
      """
        .trimIndent()
    )
  getTranslations.setString(1, bookId)
  return getTranslations.executeQuery().map {
    val language = getString("language")
    Translation(
      bookId = bookId,
      language = language,
      title = getString("title"),
      author = getAuthorName(authorCode, language),
    )
  }
}

fun Connection.insertTranslation(book: String, language: String, title: String) {
  val upsertTranslation =
    prepareStatement(
      """
      INSERT INTO translations(book, language, title) VALUES (?, ?, ?)
      ON CONFLICT DO NOTHING
    """
        .trimIndent()
    )
  upsertTranslation.setString(1, book)
  upsertTranslation.setString(2, language)
  upsertTranslation.setString(3, title)
  upsertTranslation.executeUpdate()
}

@Serializable
data class Translation(
  val bookId: String,
  val language: String,
  val title: String,
  val author: String,
)
