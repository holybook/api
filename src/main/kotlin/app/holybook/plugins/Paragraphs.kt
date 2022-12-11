package app.holybook.plugins

import app.holybook.models.Books
import app.holybook.models.Paragraphs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

val client = HttpClient()

fun Routing.configureParagraphs() {
    post("books/{id}/paragraphs") {
        val content = call.receive<BookContent>()
        val paragraphContent = client.get(content.sourceUrl)
        val contentType = paragraphContent.contentType()

        val skipStart = call.request.queryParameters["skipStart"]?.toInt() ?: 0
        val skipEnd = call.request.queryParameters["skipStart"]?.toInt() ?: 0

        val paragraphs =
            getParagraphs(contentType, paragraphContent.body())
        if (paragraphs != null) {
            val filteredParagraphs = paragraphs.subList(
                fromIndex = skipStart,
                toIndex = paragraphs.size - skipEnd
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
}

fun getParagraphs(
    contentType: ContentType?,
    content: ByteArray
): List<String>? {
    if (contentType?.match("application/pdf") == true) {
        // Handle PDF.
        return parsePdf(content)
    }

    return null
}

fun parsePdf(content: ByteArray): List<String> {
    val pdfDocument = PDDocument.load(content)
    val pdfTextStripper = PDFTextStripper()
    pdfTextStripper.paragraphStart = "/t";
    pdfTextStripper.sortByPosition = true;
    val text = pdfTextStripper.getText(pdfDocument)

    return text.split(pdfTextStripper.paragraphStart)
}

@Serializable
data class BookContent(val sourceUrl: String, val language: String)

@Serializable
data class AddParagraphsResponse(val paragraphsAdded: Int)
