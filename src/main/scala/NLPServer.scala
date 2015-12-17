import org.json4s.{Formats, DefaultFormats}
import spray.routing.SimpleRoutingApp
import spray.httpx.Json4sSupport
import akka.actor.ActorSystem
import processors._

// for handling incoming json containing the "text" field
object MessageProtocol extends Json4sSupport {

  override implicit def json4sFormats: Formats = DefaultFormats

  case class Message(text: String)

}

/**
 *
 */
object NLPServer extends App with SimpleRoutingApp {
  // pre-load models before taking requests
  TextProcessor.proc.annotate("blah")
  implicit val system = ActorSystem("parser-courier")
  import MessageProtocol._

  startServer(interface = "localhost", port = 8888) {
    post {
      path("parse") {
        entity(as[Message]) { m =>
          println(s"Received POST with text -> ${m.text}")
          val doc = TextProcessor.toAnnotatedDocument(m.text)
          // case class is implicitly converted to json
          complete(doc)
          }
      }
    }
  }
}