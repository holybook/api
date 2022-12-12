package app.holybook.plugins

import app.holybook.models.Paragraphs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

val client = HttpClient()

fun Routing.configureParagraphs() {
    get("books/{id}/paragraphs") {
        getParagraphs()
    }

    post("books/{id}/paragraphs") {
        postParagraphs()
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

suspend fun PipelineContext<Unit, ApplicationCall>.postParagraphs() {
    val content = call.receive<BookContent>()
    val paragraphContent = client.get(content.sourceUrl)
    val contentType = paragraphContent.contentType()

    val skipStart = call.request.queryParameters["skipStart"]?.toInt() ?: 0
    val skipEnd = call.request.queryParameters["skipStart"]?.toInt() ?: 0

    val paragraphs = parseParagraphs(contentType, paragraphContent.body())
    if (paragraphs != null) {
        val filteredParagraphs = paragraphs.subList(
            fromIndex = skipStart, toIndex = paragraphs.size - skipEnd
        )
        transaction {
            filteredParagraphs.forEachIndexed { i, paragraph ->
                Paragraphs.insert {
                    it[bookId] = call.parameters["id"]?.toInt()!!
                    it[index] = i
                    it[language] = content.language
                    it[text] = paragraph
                }
            }
        }
        call.respond(
            HttpStatusCode.OK,
            AddParagraphsResponse(paragraphsAdded = filteredParagraphs.size)
        )
    } else {
        call.respond(HttpStatusCode.NotImplemented)
    }
}

val parsers = listOf(PdfParser())

fun parseParagraphs(
    contentType: ContentType?, content: ByteArray
): List<String>? {
    for (parser in parsers) {
        if (contentType?.match(parser.contentType) == true) {
            return parser.parse(content)
        }
    }

    return null
}

@Serializable
data class BookContent(val sourceUrl: String, val language: String)

@Serializable
data class AddParagraphsResponse(val paragraphsAdded: Int)

@Serializable
data class Paragraph(val index: Int, val text: String)
