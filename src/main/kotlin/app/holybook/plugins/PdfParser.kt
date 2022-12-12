package app.holybook.plugins

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

class PdfParser : ParagraphParser {
    override val contentType: String
        get() = "application/pdf"

    override fun parse(content: ByteArray): List<String> {
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

        return text.split(pdfTextStripper.paragraphStart).map {
            it.trim()
        }.filter {
            it.isNotEmpty()
        }
    }
}