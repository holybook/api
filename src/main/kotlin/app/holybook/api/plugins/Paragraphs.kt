package app.holybook.api.plugins

import app.holybook.api.models.Paragraphs
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.configureParagraphs() {
    get("books/{id}/paragraphs") {
        getParagraphs()
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getParagraphs() {
    val bookId = call.parameters["id"]?.toInt()

    if (bookId == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }

    val startIndex = call.request.queryParameters["start"]?.toInt()
    val endIndex = call.request.queryParameters["end"]?.toInt()

    val paragraphs = transaction {
        Paragraphs.select {
            var filter = Paragraphs.bookId eq bookId
            if (startIndex != null) {
                filter = filter and (Paragraphs.index greaterEq startIndex)
            }
            if (endIndex != null) {
                filter = filter and (Paragraphs.index lessEq endIndex)
            }
            filter
        }.orderBy(Paragraphs.index).map {
            Paragraph(it[Paragraphs.index], it[Paragraphs.text])
        }
    }
    call.respond(paragraphs)
}

@Serializable
data class BookContent(val sourceUrl: String, val language: String)

@Serializable
data class AddParagraphsResponse(val paragraphsAdded: Int)

@Serializable
data class Paragraph(val index: Int, val text: String)
