package app.holybook.import.parsers

import app.holybook.import.BookContent
import app.holybook.import.Paragraph
import io.ktor.http.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

class PdfParser : ParagraphParser {
    override fun matches(contentType: ContentType?, url: String): Boolean {
        return contentType?.match("application/pdf") == true
    }

    override fun parse(content: ByteArray): BookContent {
        val pdfDocument = PDDocument.load(content)
        val pdfTextStripper = PDFTextStripper()
        pdfTextStripper.paragraphStart = "/t"
        pdfTextStripper.sortByPosition = true
        val originalText = pdfTextStripper.getText(pdfDocument)
        val text = originalText
            .replace('\n', ' ')
            .replace(Regex("[^!.?]${pdfTextStripper.paragraphStart}[a-z]")) {
                it.value.replace(pdfTextStripper.paragraphStart, "")
            }

        val paragraphs =
            text.split(pdfTextStripper.paragraphStart).map { it.trim() }
                .filter { it.isNotEmpty() }
        val mergedParagraphs = mutableListOf<String>()
        var pendingParagraph = ""
        for (p in paragraphs) {
            if (p.isFullParagraph()) {
                mergedParagraphs.add(pendingParagraph + p)
                pendingParagraph = ""
            } else {
                pendingParagraph += p
            }
        }

        return BookContent(
            title = "",
            author = "",
            paragraphs = mergedParagraphs.map { Paragraph(it, "body") }
        )
    }

    private fun String.endsWithStop(): Boolean {
        return endsWith('.') || endsWith('!') || endsWith('?')
    }

    private fun String.isFullParagraph(): Boolean {
        return endsWithStop() || isTitle()
    }

    private fun String.isTitle(): Boolean {
        return length < 256 && get(0).isUpperCase()
    }
}