import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.html.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.FileInputStream
import javax.xml.stream.XMLInputFactory

val logger = LoggerFactory.getLogger("server")
val client = HttpClient(CIO)

fun HTML.index(sentences: List<String>) {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            sentences.map { s ->
                div {
                    +s
                }
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(StatusPages) {
            exception<Throwable> { cause ->
                logger.error("Unexpected error", cause)
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                throw cause
            }
        }
        install(CallLogging) {
            level = Level.INFO
        }
        routing {
            get("/") {
                val response: HttpResponse =
                    client.request("https://www.bahai.org/library/authoritative-texts/bahaullah/kitab-i-aqdas/kitab-i-aqdas.xhtml?9870f79a") {
                        // Configure request parameters exposed by HttpRequestBuilder
                    }

                val xmlInputFactory = XMLInputFactory.newInstance()
                val reader = xmlInputFactory.createXMLEventReader(response.content.toInputStream())

                var event = reader.nextEvent()
                var isParagraph = false
                val text = mutableListOf<String>()
                while (event != null) {
                    if (event.isStartElement) {
                        val element = event.asStartElement()
                        if (element.name.localPart == "p") {
                            isParagraph = true
                            continue
                        }
                    }
                    if (event.isCharacters && isParagraph) {
                        text += event.asCharacters().data
                    }

                    isParagraph = false
                    event = reader.nextEvent()
                }

                call.respondHtml {
                    index(text.flatMap { paragraph -> paragraph.split(".") })
                }

//                call.respondBytes(ContentType.Text.Html) {
//                    val responseContent = ByteBuffer.allocate(100_000)
//                    response.content.readFully(responseContent)
//                    responseContent.array()
//                }
            }
        }
    }.start(wait = true)
}