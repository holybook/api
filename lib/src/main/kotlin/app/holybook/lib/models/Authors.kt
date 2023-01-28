package app.holybook.lib.models

import java.sql.Connection
import java.sql.Date
import java.sql.Statement

fun Connection.createAuthorTables() {
  createStatement()
    .executeUpdate(
      """
    CREATE TABLE IF NOT EXISTS authors (
        id SERIAL PRIMARY KEY
    );
    
    CREATE TABLE IF NOT EXISTS author_names (
        id SERIAL NOT NULL REFERENCES authors,
        language VARCHAR(3) NOT NULL,
        name VARCHAR(256) NOT NULL,
        
        
        PRIMARY KEY (id, language)
    );
  """
        .trimIndent()
    )
}

fun Connection.dropAuthorTables() {
  createStatement()
    .executeUpdate(
      """
    DROP TABLE IF EXISTS author_names;
    DROP TABLE IF EXISTS authors;
  """
        .trimIndent()
    )
}

fun Connection.findAuthorIdByName(name: String, language: String): Int? {
  val getAuthorIdByName =
    prepareStatement(
      """
        SELECT id FROM author_names WHERE language = ? AND name = ?
      """
        .trimIndent()
    )
  getAuthorIdByName.setString(1, language)
  getAuthorIdByName.setString(2, name)
  val results = getAuthorIdByName.executeQuery()
  if (!results.next()) {
    return null
  }

  return results.getInt("id")
}

fun Connection.insertAuthor(name: String, language: String): Int {
  val id =
    createStatement()
      .executeUpdate("INSERT INTO authors(id) VALUES(DEFAULT)", Statement.RETURN_GENERATED_KEYS)

  val preparedStatement = prepareStatement("""
      INSERT INTO author_names(id, language, name) VALUES (?, ?, ?) ON CONFLICT DO NOTHING
    """.trimIndent())

  preparedStatement.setInt(1, id)
  preparedStatement.setString(2, language)
  preparedStatement.setString(3, name)
  preparedStatement.executeUpdate()

  return id
}
