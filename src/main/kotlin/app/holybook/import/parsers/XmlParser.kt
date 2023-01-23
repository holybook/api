package app.holybook.import.parsers

import app.holybook.import.BookContent
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.http.*

class XmlParser<T>(
    private val hostName: String,
    private val parser: Konsumer.() -> T
) : ParagraphParser<T> {
    override fun matches(contentType: ContentType?, url: Url): Boolean {
        return (contentType?.match("application/xml") == true || contentType?.match(
            "application/xhtml+xml"
        ) == true) && url.host == hostName
    }

    override fun parse(content: ByteArray): T {
        return String(content).konsumeXml().parser()
    }

}