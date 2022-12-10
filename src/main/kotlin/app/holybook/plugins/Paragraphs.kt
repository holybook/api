package app.holybook.plugins

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

val client = HttpClient()

fun Routing.configureParagraphs() {
    post("books/{id}/paragraphs") {
        val content = call.receive<BookContent>()
        val response = client.get(content.sourceUrl)
        val contentType = response.contentType()

        if (contentType?.match("application/pdf") == true) {
            // Handle PDF.
            val paragraphs = parsePdf(response.body())
            System.err.println(paragraphs)
            call.respond(HttpStatusCode.OK)
            return@post
        }

        call.respond(HttpStatusCode.NotImplemented)
    }
}

fun parsePdf(content: ByteArray): Iterable<String> {
    val pdfDocument = PDDocument.load(content)
    val pdfTextStripper = PDFTextStripper()
    val text = pdfTextStripper.getText(pdfDocument)

    return text.split('\n')
}

@Serializable
data class BookContent(val sourceUrl: String)
