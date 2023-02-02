package app.holybook.import.sources

import app.holybook.import.model.ContentDescriptor
import app.holybook.import.model.SourceDescriptor
import app.holybook.import.network.Http.client
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import java.io.Writer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

object SourceFetcher {

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

  suspend fun fetchSources(cacheDirectory: Path): Flow<List<ContentDescriptor>> = flow {
    fetchers.forEach { source ->
      log.info("Fetching sources from ${source.url}")
      val response = client.get(source.url)
      val contentDescriptors = source.parser(response.body())

      Files.createDirectories(cacheDirectory)
      val cacheFilePath =
        FileSystems.getDefault().getPath(cacheDirectory.absolutePathString() + "/${source.fileName}")
      cacheFilePath.outputStream().bufferedWriter().writeDescriptors(contentDescriptors)

      emit(contentDescriptors)
    }
  }

  private fun Writer.writeDescriptors(descriptors: List<ContentDescriptor>) {
    descriptors.forEach {
      writeDescriptor(it)
    }
    flush()
  }

  private fun Writer.writeDescriptor(descriptor: ContentDescriptor) {
    write("${descriptor.language} ${descriptor.id} ${descriptor.url}\n")
  }
}


