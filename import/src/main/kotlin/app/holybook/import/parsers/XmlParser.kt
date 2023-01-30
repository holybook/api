package app.holybook.import.parsers

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.http.ContentType
import io.ktor.http.Url
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class XmlParser<T>(
  private val urlMatcher: UrlMatcher,
  private val parser: Konsumer.() -> T,
) : ContentParser<T> {
  override fun matches(contentType: ContentType?, url: Url): Boolean {
    return (contentType?.match("application/xml") == true ||
      contentType?.match("application/xhtml+xml") == true) && urlMatcher.matches(url)
  }

  override fun parse(content: ByteArray): T {
    return String(content).konsumeXml().parser()
  }
}

class XmlDOMParser<T>(private val urlMatcher: UrlMatcher, private val transform: (Node) -> T) :
  ContentParser<T> {

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

  override fun matches(contentType: ContentType?, url: Url): Boolean {
    return (contentType?.match("application/xml") == true ||
      contentType?.match("application/xhtml+xml") == true) && urlMatcher.matches(url)
  }

  override fun parse(content: ByteArray): T {
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    transform(documentBuilder.parse(content.inputStream()).documentElement)
  }
}

fun <T> Node.transform(xPathExpression: String, transform: (List<Node>) -> T): T {
  val xpath = XPathFactory.newInstance().newXPath()
  val nodes = xpath.evaluate(xPathExpression, this, XPathConstants.NODESET) as NodeList

  val nodeList = mutableListOf<Node>()
  for (i in 0 until nodes.length) {
    nodeList.add(nodes.item(i))
  }
  return transform(nodeList)
}

interface UrlMatcher {
  fun matches(url: Url): Boolean
}

class UrlPrefixMatcher(private val hostName: String, private val pathPrefix: String = "") :
  UrlMatcher {
  override fun matches(url: Url) = url.host == hostName && url.encodedPath.startsWith(pathPrefix)
}
