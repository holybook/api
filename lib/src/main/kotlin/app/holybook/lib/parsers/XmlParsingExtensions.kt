package app.holybook.lib.parsers

import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import javax.xml.transform.OutputKeys

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
