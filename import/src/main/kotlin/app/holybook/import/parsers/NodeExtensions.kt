package app.holybook.import.parsers

import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList

private fun <T> Node.transformXPath(xPathExpression: String, transform: (List<Node>) -> T): T {
  val xpath = XPathFactory.newInstance().newXPath()
  val nodes = xpath.evaluate(xPathExpression, this, XPathConstants.NODESET) as NodeList

  val nodeList = mutableListOf<Node>()
  for (i in 0 until nodes.length) {
    nodeList.add(nodes.item(i))
  }
  return transform(nodeList)
}

fun Node.getStringList(xPathExpression: String) =
  transformXPath(xPathExpression) { nodes -> nodes.map { it.nodeValue } }
