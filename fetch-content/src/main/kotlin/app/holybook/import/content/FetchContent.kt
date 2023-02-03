package app.holybook.import.content

import app.holybook.import.content.parsers.BibliothekBahaiDe
import app.holybook.import.content.parsers.ReferenceLibrary
import app.holybook.lib.models.BookContent
import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.network.Http.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import org.slf4j.LoggerFactory

private val xml = XML {}
private val log = LoggerFactory.getLogger("fetch-content")
val rules = listOf(ReferenceLibrary.rule, BibliothekBahaiDe.rule)

fun parseParagraphs(contentType: ContentType?, url: Url, content: ByteArray): BookContent? {
  for (rule in rules) {
    if (rule.matcher.matches(contentType, url)) {
      return rule.parser(content)
    }
  }

  return null
}

suspend fun fetchContent(descriptor: ContentDescriptor): BookContent {
  log.info("Fetching content from ${descriptor.url}")
  val paragraphContent = client.get(descriptor.url)
  val contentType = paragraphContent.contentType()

  return parseParagraphs(contentType, Url(descriptor.url), paragraphContent.body())
    ?: throw IOException("Could not parse content from url ${descriptor.url}")
}

suspend fun fetchAll(descriptors: List<ContentDescriptor>, targetDirectory: Path) {
  descriptors.forEach {
    val content = fetchContent(it)
    content.writeToDisk(getFilePath(targetDirectory, content, it))
  }
}

fun BookContent.writeToDisk(path: Path) {
  Files.createDirectories(path.parent)
  val writer = XmlStreaming.newWriter(path.outputStream(), "UTF-8")
  xml.encodeToWriter(writer, this, null)
}

fun getFilePath(targetDirectory: Path, content: BookContent, descriptor: ContentDescriptor): Path {
  return FileSystems.getDefault()
    .getPath(
      targetDirectory.absolutePathString(),
      "/${descriptor.language}/${content.author}/${descriptor.id}.xml"
    )
}
