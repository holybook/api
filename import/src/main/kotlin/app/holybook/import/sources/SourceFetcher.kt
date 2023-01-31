package app.holybook.import.sources

import app.holybook.import.model.SourceDescriptor
import app.holybook.import.network.Http.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import org.slf4j.LoggerFactory

private val fetchers =
  listOf(
    SourceDescriptor(
      Url(
        "https://www.bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/"
      ),
      "en.uhj.txt",
      ReferenceLibrary.uhjList
    )
  )
private val log = LoggerFactory.getLogger("fetch-sources")

suspend fun fetchSources(targetDirectory: Path) {
  fetchers.forEach { source ->
    log.info("Fetching sources from ${source.url}")
    val response = client.get(source.url)
    val contentDescriptors = source.parser(response.body())

    log.info(contentDescriptors.toString())

    val filePath =
      FileSystems.getDefault().getPath(targetDirectory.absolutePathString() + "/${source.fileName}")
  }
}
