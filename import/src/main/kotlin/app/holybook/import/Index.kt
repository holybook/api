@file:OptIn(ExperimentalSerializationApi::class)

package app.holybook.import

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

suspend fun fetchAndImportIndex() {
  val index: List<BookInfo> =
    Json.decodeFromStream(Json::class.java.getResourceAsStream("/index.json"))

  createDatabase()
  index.forEach { fetchAndImportBook(it) }
}

@Serializable
data class BookInfo(
  val id: String,
  val original: ContentInfo,
  val translations: List<ContentInfo> = listOf(),
)
