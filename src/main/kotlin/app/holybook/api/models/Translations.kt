package app.holybook.api.models

import app.holybook.api.db.map
import io.ktor.http.content.LastModifiedVersion
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

fun Connection.getTranslations(bookId: String): List<Translation> {
  val getTranslations = prepareStatement("""
        SELECT language, title FROM translations WHERE book = ?
      """.trimIndent())
  getTranslations.setString(1, bookId)
  return getTranslations.executeQuery().map {
    Translation(
      getString("language"),
      getString("title")
    )
  }
}

fun Connection.getTranslationLastModified(bookId: String, language: String): LocalDateTime? {
  val getExistingTranslation = prepareStatement("""
      SELECT last_modified FROM translations WHERE book = ? AND language = ?
    """.trimIndent())
  getExistingTranslation.setString(1, bookId)
  getExistingTranslation.setString(2, language)
  val existingTranslation = getExistingTranslation.executeQuery()

  if (!existingTranslation.next()) {
    return null
  }

  return existingTranslation.getTimestamp("last_modified").toLocalDateTime()
}

fun Connection.upsertTranslation(
  book: String,
  language: String,
  title: String,
  lastModified: LocalDateTime,
) {
  val upsertTranslation = prepareStatement("""
      INSERT INTO translations(book, language, title, last_modified) VALUES (?, ?, ?, ?)
      ON CONFLICT (book, language) DO UPDATE SET 
        title = ?,
        last_modified = ? 
    """.trimIndent())
  val lastModifiedTimestamp = Timestamp.valueOf(lastModified)
  upsertTranslation.setString(1, book)
  upsertTranslation.setString(2, language)
  upsertTranslation.setString(3, title)
  upsertTranslation.setTimestamp(4, lastModifiedTimestamp)
  upsertTranslation.setString(5, title)
  upsertTranslation.setTimestamp(6, lastModifiedTimestamp)
  upsertTranslation.executeUpdate()
}

@Serializable
data class Translation(val language: String, val title: String)