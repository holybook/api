package app.holybook.lib.models

import kotlinx.serialization.Serializable

private val universalHouseOfJustice =
  Author(
    "uhj",
    names =
      mapOf(
        "en" to "The Universal House of Justice",
        "de" to "Das Universale Haus der Gerechtigkeit"
      )
  )

val authors = listOf(universalHouseOfJustice).associateBy { it.id }

@Serializable
data class Author(val id: String, val names: Map<String, String>)

fun getAuthorName(authorCode: String, language: String): String? =
  authors[authorCode]?.names?.get(language)
