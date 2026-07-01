package app.holybook.lib.models

import app.holybook.lib.parsers.readDocument
import app.holybook.lib.parsers.writeDocument
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class BookContentTest {

  private fun parse(xml: String): BookContent =
    ByteArrayInputStream(xml.toByteArray()).readDocument().toBookContent()

  /** Convenience: the displayed label of a paragraph, e.g. `1.1:5` or (flat) `5`. */
  private fun Paragraph.label(): String =
    if (number == null) "" else if (sectionPath.isEmpty()) "$number" else "$sectionPath:$number"

  @Test
  fun `flat book keeps bare numbering and empty section paths`() {
    val book =
      parse(
        """
        <book id="x" language="en" title="T" author="uhj">
          <p type="letter-head">Head</p>
          <p>First.</p>
          <p>Second.</p>
        </book>
        """
          .trimIndent()
      )

    val body = book.paragraphs.filter { it.type == ParagraphType.BODY }
    assertEquals(listOf("1", "2"), body.map { it.label() })
    assertEquals(listOf("", ""), body.map { it.sectionPath })
    assertEquals(0, book.paragraphs.count { it.type == ParagraphType.SECTION_TITLE })
  }

  @Test
  fun `nested sections restart numbering per section`() {
    val book =
      parse(
        """
        <book id="x" language="en" title="T" author="abu">
          <section title="One">
            <p>a</p>
            <p>b</p>
            <section title="One-one">
              <p>c</p>
            </section>
            <p>d</p>
          </section>
          <section title="Two">
            <p>e</p>
          </section>
        </book>
        """
          .trimIndent()
      )

    // Section titles carry their own path and no number.
    val titles = book.paragraphs.filter { it.type == ParagraphType.SECTION_TITLE }
    assertEquals(listOf("One" to "1", "One-one" to "1.1", "Two" to "2"), titles.map { it.text to it.sectionPath })

    // Body paragraphs: 1:1, 1:2, 1.1:1, then back to section 1 -> 1:3, then 2:1.
    val body = book.paragraphs.filter { it.type == ParagraphType.BODY }
    assertEquals(listOf("1:1", "1:2", "1.1:1", "1:3", "2:1"), body.map { it.label() })
    assertEquals(listOf("a", "b", "c", "d", "e"), body.map { it.text })
  }

  @Test
  fun `xml serialization round-trips through the flat model`() {
    val xml =
      """
      <book id="x" language="en" title="T" author="abu">
        <section title="One">
          <p type="body">a</p>
          <section title="One-one">
            <p type="body">b</p>
          </section>
          <p type="body">c</p>
        </section>
      </book>
      """
        .trimIndent()

    val once = parse(xml)
    val serialized = ByteArrayOutputStream().apply { writeDocument(once.toXmlDocument()) }.toByteArray()
    val twice = ByteArrayInputStream(serialized).readDocument().toBookContent()

    assertEquals(once.paragraphs, twice.paragraphs)
  }
}
