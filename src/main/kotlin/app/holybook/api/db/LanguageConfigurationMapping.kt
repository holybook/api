package app.holybook.api.db

import kotlin.collections.mapOf

private val languageConfigurationMapping = mapOf(
  "en" to "english",
  "de" to "german"
)

fun getLanguageConfiguration(languageCode: String) =
  languageConfigurationMapping[languageCode] ?: "simple"