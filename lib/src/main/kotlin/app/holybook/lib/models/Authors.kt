package app.holybook.lib.models

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.map
import java.sql.Connection
import kotlinx.serialization.Serializable

@Serializable data class Author(val id: String, val names: Map<String, String>)

/**
 * Known authors, keyed by their stable code. This is the *seed* registry: it is used to populate the
 * `authors` table during import and as a fallback when the table is empty or unavailable. The
 * runtime source of truth is the database (see [getAuthorName] / [getAllAuthors]).
 */
val seedAuthors: Map<String, Author> =
  listOf(
      Author(
        "bab",
        mapOf("en" to "The Báb", "de" to "Der Báb"),
      ),
      Author(
        "bahaullah",
        mapOf("en" to "Bahá’u’lláh", "de" to "Bahá’u’lláh"),
      ),
      Author(
        "abdulbaha",
        mapOf("en" to "‘Abdu’l-Bahá", "de" to "‘Abdu’l-Bahá"),
      ),
      Author(
        "shoghieffendi",
        mapOf("en" to "Shoghi Effendi", "de" to "Shoghi Effendi"),
      ),
      Author(
        "uhj",
        mapOf(
          "en" to "The Universal House of Justice",
          "de" to "Das Universale Haus der Gerechtigkeit",
        ),
      ),
      Author(
        "compilation",
        mapOf("en" to "Compilations", "de" to "Zusammenstellungen"),
      ),
    )
    .associateBy { it.id }

fun Connection.createAuthorsTable() {
  createStatement()
    .executeUpdate(
      """
    CREATE TABLE IF NOT EXISTS authors (
        id VARCHAR(32) NOT NULL,

        PRIMARY KEY (id)
    );

    CREATE TABLE IF NOT EXISTS author_names (
        author VARCHAR(32) NOT NULL,
        language VARCHAR(3) NOT NULL,
        name VARCHAR(256) NOT NULL,

        PRIMARY KEY (author, language),
        FOREIGN KEY (author) REFERENCES authors(id)
    );
  """
        .trimIndent()
    )
}

fun Connection.dropAuthorsTable() {
  createStatement()
    .executeUpdate(
      """
    DROP TABLE IF EXISTS author_names;
    DROP TABLE IF EXISTS authors;
  """
        .trimIndent()
    )
}

fun Connection.insertAuthor(author: Author) {
  prepareStatement("INSERT INTO authors(id) VALUES (?) ON CONFLICT DO NOTHING").apply {
    setString(1, author.id)
    executeUpdate()
  }
  val insertName =
    prepareStatement(
      """
        INSERT INTO author_names(author, language, name) VALUES (?, ?, ?)
        ON CONFLICT (author, language) DO UPDATE SET name = EXCLUDED.name
      """
        .trimIndent()
    )
  author.names.forEach { (language, name) ->
    insertName.setString(1, author.id)
    insertName.setString(2, language)
    insertName.setString(3, name)
    insertName.addBatch()
  }
  insertName.executeBatch()
}

/** Populates the `authors`/`author_names` tables from the [seedAuthors] registry. */
fun Connection.seedAuthors() = seedAuthors.values.forEach { insertAuthor(it) }

private fun Connection.loadAuthors(): Map<String, Author> {
  val names =
    prepareStatement("SELECT author, language, name FROM author_names").executeQuery().map {
      Triple(getString("author"), getString("language"), getString("name"))
    }
  return names
    .groupBy { it.first }
    .mapValues { (id, rows) -> Author(id, rows.associate { it.second to it.third }) }
}

/**
 * Process-wide cache of the author registry, loaded lazily from the database and merged over
 * [seedAuthors]. Authors change only on import (a separate process that rebuilds the schema), so a
 * one-time load is sufficient for a server run.
 */
object AuthorRegistry {
  @Volatile private var cache: Map<String, Author>? = null

  fun all(): Map<String, Author> {
    cache?.let {
      return it
    }
    val loaded =
      try {
        transaction { loadAuthors() }
      } catch (e: Exception) {
        emptyMap()
      }
    return (seedAuthors + loaded).also { cache = it }
  }

  fun name(authorCode: String, language: String): String =
    all()[authorCode]?.names?.get(language)
      ?: throw IllegalArgumentException(
        "Could not find an author name for $authorCode in language $language"
      )
}

fun getAuthorName(authorCode: String, language: String): String =
  AuthorRegistry.name(authorCode, language)

fun getAllAuthors(): List<Author> = AuthorRegistry.all().values.toList()
