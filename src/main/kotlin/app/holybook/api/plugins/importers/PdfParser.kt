package app.holybook.api.plugins.importers

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

        val paragraphs = text.split(pdfTextStripper.paragraphStart).map { it.trim() }.filter { it.isNotEmpty() }
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

        return mergedParagraphs
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