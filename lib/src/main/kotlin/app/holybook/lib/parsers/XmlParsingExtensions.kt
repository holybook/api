package app.holybook.lib.parsers

import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

private val transformerFactory = TransformerFactory.newInstance()

fun buildDocument(body: (Document) -> Unit): Document {
  val builder = documentBuilderFactory.newDocumentBuilder()
  val document = builder.newDocument()
  body(document)
  return document
}

fun OutputStream.writeDocument(document: Document) {
  val transformer = transformerFactory.newTransformer()
  transformer.setOutputProperty(OutputKeys.INDENT, "yes")
  val source = DOMSource(document)
  val result = StreamResult(this)

  transformer.transform(source, result)
  close()
}

fun InputStream.readDocument(): Document {
  val document = documentBuilderFactory.newDocumentBuilder().parse(this)
  close()
  return document
}

fun <T> NodeList.map(body: (Node) -> T): List<T> {
  val result = mutableListOf<T>()
  for (i in 0 until length) {
    result.add(body(item(i)))
  }
  return result
}

fun Node.getAttributeOrNull(name: String) = attributes.getNamedItem(name)?.nodeValue
