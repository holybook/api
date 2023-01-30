package app.holybook.import.parsers

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.http.*
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

class XmlParser<T>(
    private val urlMatcher: UrlMatcher,
    private val parser: Konsumer.() -> T,
) : ContentParser<T> {
    override fun matches(contentType: ContentType?, url: Url): Boolean {
        return (contentType?.match("application/xml") == true || contentType?.match(
            "application/xhtml+xml"
        ) == true) && urlMatcher.matches(url)
    }

    override fun parse(content: ByteArray): T {
        return String(content).konsumeXml().parser()
    }

}

class XmlXPathParser<T>(
    private val urlMatcher: UrlMatcher,
    private val xPathExpression: String,
    private val transform: (List<Node>) -> T,
): ContentParser<T> {
    override fun matches(contentType: ContentType?, url: Url): Boolean {
        return (contentType?.match("application/xml") == true || contentType?.match(
            "application/xhtml+xml"
        ) == true) && urlMatcher.matches(url)
    }

    override fun parse(content: ByteArray): T {
        val xpath = XPathFactory.newInstance().newXPath()
        val inputSource = InputSource(content.inputStream())
        val nodes = xpath.evaluate(xPathExpression, inputSource, XPathConstants.NODESET) as NodeList

        val nodeList = mutableListOf<Node>()
        for (i in 0 until nodes.length) {
            nodeList.add(nodes.item(i))
        }
        return transform(nodeList)
    }
}

interface UrlMatcher {
    fun matches(url: Url): Boolean
}

class UrlPrefixMatcher(private val hostName: String, private val pathPrefix: String = "") : UrlMatcher {
    override fun matches(url: Url) = url.host == hostName && url.encodedPath.startsWith(pathPrefix)
}