package app.holybook.import.sources

import app.holybook.import.sources.model.SourceDescriptor
import app.holybook.import.sources.parsers.BibliothekBahaiDe
import app.holybook.import.sources.parsers.ReferenceLibrary
import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.models.writeDescriptor
import app.holybook.lib.network.Http.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import java.io.Writer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import org.slf4j.LoggerFactory

object SourceFetcher {

  /**
   * Maps each bahai.org author index path segment to our internal author code. The Universal House
   * of Justice is intentionally excluded here: its works are messages enumerated separately (see
   * [ReferenceLibrary.uhjList]) rather than a flat list of works.
   */
  private val referenceLibraryAuthors =
    mapOf(
      "the-bab" to "bab",
      "bahaullah" to "bahaullah",
      "abdul-baha" to "abdulbaha",
      "shoghi-effendi" to "shoghieffendi",
      "compilations" to "compilation",
    )

  private val fetchers =
    listOf(
      SourceDescriptor(
        Url("https://bibliothek.bahai.de/pubAuthor.php?authorCode=uhj"),
        "de.uhj.txt",
        BibliothekBahaiDe.uhjList,
      ),
      SourceDescriptor(
        Url(
          "https://www.bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/"
        ),
        "en.uhj.txt",
        ReferenceLibrary.uhjList,
      ),
    ) +
      referenceLibraryAuthors.map { (urlAuthor, authorCode) ->
        SourceDescriptor(
          Url("https://www.bahai.org/library/authoritative-texts/$urlAuthor/"),
          "en.$authorCode.txt",
          ReferenceLibrary.workList(urlAuthor, authorCode),
        )
      }
  private val log = LoggerFactory.getLogger("fetch-sources")

  suspend fun fetchSources(outputDirectory: Path) {
    fetchers.forEach { source ->
      log.info("Fetching sources from ${source.url}")
      val response = client.get(source.url)
      val contentDescriptors = source.parser(response.body())
      log.info(" - Extracted ${contentDescriptors.size} content descriptors")

      Files.createDirectories(outputDirectory)
      val cacheFilePath =
        FileSystems.getDefault()
          .getPath(outputDirectory.absolutePathString() + "/${source.fileName}")
      log.info(" - Writing descriptors to $cacheFilePath")
      cacheFilePath.outputStream().bufferedWriter().writeDescriptors(contentDescriptors)
    }
  }

  private fun Writer.writeDescriptors(descriptors: List<ContentDescriptor>) {
    descriptors.forEach { writeDescriptor(it) }
    flush()
  }
}
