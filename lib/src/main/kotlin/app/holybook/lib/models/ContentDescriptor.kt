package app.holybook.lib.models

import app.holybook.lib.serialization.DateSerializer
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable

/** Describes the location of an original document containing the data of a book. */
@Serializable
data class ContentDescriptor(
  val id: String,
  val language: String,
  val authorCode: String,
  val url: String,
  /**
   * Publication date when the source provides one explicitly. When absent it is inferred for UHJ
   * messages from their date-encoded id (e.g. `uhj20200722_001`); other authors simply have no date.
   */
  @Serializable(with = DateSerializer::class) val explicitPublishedAt: LocalDate? = null,
) {
  val publishedAt: LocalDate?
    get() = explicitPublishedAt ?: inferUhjDate()

  private fun inferUhjDate(): LocalDate? =
    if (authorCode == "uhj") {
      LocalDate.parse(id.substring(3, 11), DateTimeFormatter.BASIC_ISO_DATE)
    } else {
      null
    }
}

fun readContentDescriptors(input: InputStream): List<ContentDescriptor> {
  val resultList = mutableListOf<ContentDescriptor>()
  val reader = LineNumberReader(InputStreamReader(input))
  var line: String? = reader.readLine()
  while (line != null) {
    resultList.add(line.readContentDescriptor())
    line = reader.readLine()
  }
  return resultList
}

fun String.readContentDescriptor(): ContentDescriptor {
  val parts = split(' ')
  return ContentDescriptor(
    language = parts[0],
    authorCode = parts[1],
    id = parts[2],
    url = parts[3],
    // Optional 5th token: an explicit ISO publication date. Older index files omit it.
    explicitPublishedAt = parts.getOrNull(4)?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) },
  )
}

fun Writer.writeDescriptor(descriptor: ContentDescriptor) {
  val date =
    descriptor.explicitPublishedAt?.let { " ${it.format(DateTimeFormatter.ISO_DATE)}" } ?: ""
  write("${descriptor.language} ${descriptor.authorCode} ${descriptor.id} ${descriptor.url}$date\n")
}
