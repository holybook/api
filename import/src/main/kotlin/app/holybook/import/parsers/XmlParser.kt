package app.holybook.import.parsers

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import io.ktor.http.ContentType
import io.ktor.http.Url
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

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
    return transform(documentBuilder.parse(content.inputStream()).documentElement)
  }
}

interface UrlMatcher {
  fun matches(url: Url): Boolean
}

class UrlPrefixMatcher(private val hostName: String, private val pathPrefix: String = "") :
  UrlMatcher {
  override fun matches(url: Url) = url.host == hostName && url.encodedPath.startsWith(pathPrefix)
}
