package app.holybook.import.parsers

import app.holybook.api.models.Paragraph
import app.holybook.api.models.ParagraphType
import app.holybook.import.BookContent
import app.holybook.import.BookMetadata
import app.holybook.import.OriginalBook
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.textRecursively
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE

object ReferenceLibrary {

    val parser: ParagraphParser<OriginalBook> = XmlParser("bahai.org") {
        return@XmlParser parse()
    }

    private fun Konsumer.parse(): OriginalBook {
        var title = ""
        var author = ""
        var date: LocalDate? = null
        val paragraphs = mutableListOf<Paragraph>()
        child("html") {
            child("head") {
                allChildrenAutoIgnore(Names.of("meta", "title")) {
                    when (localName) {
                        "meta" -> when (attributes.getValueOrNull("name")) {
                            "author" -> author = attributes.getValue("content")
                            "date" -> date = LocalDate.parse(
                                attributes.getValue("content"),
                                BASIC_ISO_DATE
                            )
                        }

                        "title" -> {
                            title = text()
                        }
                    }
                }
            }
            child("body") {
                child("div") {
                    recursiveGetParagraphs(paragraphs)
                }
                skipContents()
            }
        }
        return OriginalBook(
            BookMetadata(author, date),
            BookContent(title, paragraphs)
        )
    }

    private fun Konsumer.recursiveGetParagraphs(paragraphs: MutableList<Paragraph>) {
        allChildrenAutoIgnore(Names.of("div", "p")) {
            when (localName) {
                "div" -> recursiveGetParagraphs(paragraphs)
                "p" -> {
                    val classNames = attributes.getValueOrNull("class")
                    paragraphs.add(
                        Paragraph(
                            paragraphs.size,
                            textRecursively(),
                            getParagraphType(classNames)
                        )
                    )
                }
            }
        }
    }
}

private fun getParagraphType(classNames: String?): ParagraphType {
    return when (classNames) {
        "c zb" -> ParagraphType.LETTER_HEAD
        "wb" -> ParagraphType.DATE
        "ub" -> ParagraphType.ADDRESSEE
        "tb" -> ParagraphType.SALUTATION
        "c cd hb" -> ParagraphType.HEADER
        "sb vb" -> ParagraphType.SEPARATOR
        "pb" -> ParagraphType.SIGNATURE
        else -> ParagraphType.BODY
    }
}
