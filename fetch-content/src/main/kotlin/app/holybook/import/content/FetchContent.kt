package app.holybook.import.content

import app.holybook.import.content.parsers.BibliothekBahaiDe
import app.holybook.import.content.parsers.ParsedBook
import app.holybook.import.content.parsers.ReferenceLibrary
import app.holybook.import.content.parsers.withIdAndLanguage
import app.holybook.lib.models.BookContent
import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.models.toXmlDocument
import app.holybook.lib.network.Http.client
import app.holybook.lib.parsers.writeDocument
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
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("fetch-content")
val rules = listOf(ReferenceLibrary.rule, BibliothekBahaiDe.rule)

fun parseParagraphs(contentType: ContentType?, url: Url, content: ByteArray): ParsedBook? {
  for (rule in rules) {
    if (rule.matcher.matches(contentType, url)) {
      return rule.parser(content)
    }
  }

  return null
}

suspend fun fetchContent(descriptor: ContentDescriptor): ParsedBook {
  log.info("Fetching content from ${descriptor.url}")
  val paragraphContent = client.get(descriptor.url)
  val contentType = paragraphContent.contentType()

  return parseParagraphs(contentType, Url(descriptor.url), paragraphContent.body())
    ?: throw IOException(
      "Could not parse content from content type $contentType and url ${descriptor.url}"
    )
}

suspend fun fetchAll(descriptors: List<ContentDescriptor>, targetDirectory: Path) {
  descriptors.forEach {
    try {
      val content = fetchContent(it).withIdAndLanguage(it.id, it.language)
      content.writeToDisk(getFilePath(targetDirectory, content, it))
    } catch (e: Throwable) {
      log.warn("Failed to process ${it.url}", e)
    }
  }
}

fun BookContent.writeToDisk(path: Path) {
  Files.createDirectories(path.parent)
  path.outputStream().writeDocument(toXmlDocument())
}

fun getFilePath(targetDirectory: Path, content: BookContent, descriptor: ContentDescriptor): Path {
  return FileSystems.getDefault()
    .getPath(
      targetDirectory.absolutePathString(),
      "/${descriptor.language}/${content.author}/${descriptor.id}.xml"
    )
}