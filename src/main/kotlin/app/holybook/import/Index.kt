@file:OptIn(ExperimentalSerializationApi::class)

package app.holybook.import

import app.holybook.api.db.Database.transaction
import app.holybook.api.models.createBooksTable
import app.holybook.api.models.createParagraphsTable
import app.holybook.api.models.createTranslationsTable
import io.ktor.util.logging.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

suspend fun fetchAndImportIndex(log: Logger) {
    val index: List<BookInfo> = Json.decodeFromStream(
        Json::class.java.getResourceAsStream("/index.json")
    )

    transaction {
        // Create tables:
        createBooksTable()
        createTranslationsTable()
        createParagraphsTable()
    }

    index.forEach {
        fetchAndImportBook(log, it)
    }
}

@Serializable
data class Index(val bookInfos: List<BookInfo>)

@Serializable
data class BookInfo(
    val id: String,
    val original: ContentInfo,
    val translations: List<ContentInfo> = listOf()
)