@file:OptIn(ExperimentalSerializationApi::class)

package app.holybook.import

import io.ktor.util.logging.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

suspend fun fetchAndImportIndex(log: Logger) {
    val index: List<BookInfo> = Json.decodeFromStream(
        Json::class.java.getResourceAsStream("/index.json")
    )
    index.forEach { bookInfo ->
        log.info("Importing from ${bookInfo.original.url}")
        fetchAndImportContent(log, bookInfo.id, bookInfo.original)
        bookInfo.translations.forEach {
            log.info("Importing from ${it.url}")
            fetchAndImportContent(log, bookInfo.id, it)
        }
    }
}

@Serializable
data class Index(val bookInfos: List<BookInfo>)

@Serializable
data class BookInfo(
    val id: String,
    val original: ContentInfo,
    val translations: List<ContentInfo>
)