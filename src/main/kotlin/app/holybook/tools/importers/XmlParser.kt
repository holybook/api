package app.holybook.tools.importers

import app.holybook.tools.BookContent
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.http.*

class XmlParser(
    private val urlPrefix: String,
    private val parser: Konsumer.() -> BookContent
) : ParagraphParser {
    override fun matches(contentType: ContentType?, url: String): Boolean {
        return contentType?.match("application/xml") == true && url.startsWith(
            urlPrefix
        )
    }

    override fun parse(content: ByteArray): BookContent {
        return String(content).konsumeXml().parser()
    }

}