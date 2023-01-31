package app.holybook.import.common

import io.ktor.http.ContentType
import io.ktor.http.Url

val CONTENT_TYPES_XML = listOf("application/xml", "application/xhtml+xml")

/**
 * Matcher that specifies whether an url and content type can be handled by an associated
 * [ContentParser].
 */
class ContentMatcher(
  private val supportedContentTypes: List<String>,
  private val hostName: String,
  private val pathPrefix: String = ""
) {
  fun matches(contentType: ContentType?, url: Url) =
    supportedContentTypes.any { contentType?.match(it) == true } &&
      url.host.endsWith(hostName) &&
      url.encodedPath.startsWith(pathPrefix)
}
