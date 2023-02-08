package app.holybook.import.content

import app.holybook.import.content.parsers.*
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
import kotlin.io.path.exists

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

suspend fun fetchContent(descriptor: ContentDescriptor): BookContent {
  log.info("Fetching content from ${descriptor.url}")
  val paragraphContent = client.get(descriptor.url)
  val contentType = paragraphContent.contentType()

  return parseParagraphs(contentType, Url(descriptor.url), paragraphContent.body())
    ?.withContentDescriptor(descriptor)
    ?: throw IOException(
      "Could not parse content from content type $contentType and url ${descriptor.url}"
    )
}

suspend fun fetchAll(descriptors: List<ContentDescriptor>, targetDirectory: Path) {
  descriptors.forEach {
    try {
      val targetFile = getFilePath(targetDirectory, it)
      if (targetFile.exists()) {
        log.info("File already exists: ${targetFile.fileName}")
        return@forEach
      }
      val content = fetchContent(it)
      content.writeToDisk(targetFile)
    } catch (e: Throwable) {
      log.warn("Failed to process ${it.url}", e)
    }
  }
}

fun BookContent.writeToDisk(path: Path) {
  Files.createDirectories(path.parent)
  path.outputStream().writeDocument(toXmlDocument())
}

fun getFilePath(targetDirectory: Path, descriptor: ContentDescriptor): Path {
  val yearFolder = descriptor.publishedAt?.let {
    "/${it.year}"
  } ?: ""
  return FileSystems.getDefault()
    .getPath(
      targetDirectory.absolutePathString(),
      "/${descriptor.language}/${descriptor.authorCode}$yearFolder/${descriptor.id}.xml"
    )
}
