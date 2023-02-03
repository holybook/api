package app.holybook.lib.models

import java.lang.IllegalArgumentException

private val authors =
  listOf(
    Author(
      "uhj",
      names =
        mapOf(
          "en" to "The Universal House of Justice",
          "de" to "Das Universale Haus der Gerechtigkeit"
        )
    )
  )

data class Author(val id: String, val names: Map<String, String>)

fun getAuthorIdByName(name: String) =
  authors.find { it.names.values.contains(name) }?.id
    ?: throw IllegalArgumentException("Could not find author with name: $name")
