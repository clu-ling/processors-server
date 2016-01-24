import org.json4s.{Formats, DefaultFormats}
import spray.http.ContentTypes
import spray.routing.SimpleRoutingApp
import spray.httpx.Json4sSupport
import akka.actor.ActorSystem
import scala.util.{Failure, Success}
import processors._

import scala.concurrent.duration._
import akka.pattern.ask
import spray.can.server.Stats
import spray.can.Http
import spray.httpx.marshalling.Marshaller
import spray.util._

// for handling incoming json containing the "text" field
object MessageProtocol extends Json4sSupport {

  override implicit def json4sFormats: Formats = DefaultFormats

  case class Message(text: String)

}

/**
 *
 */
object NLPServer extends App with SimpleRoutingApp {

  implicit val statsMarshaller: Marshaller[Stats] =
    Marshaller.delegate[Stats, String](ContentTypes.`text/plain`) { stats =>
        "Uptime                : " + stats.uptime.formatHMS + '\n' +
        "Total requests        : " + stats.totalRequests + '\n' +
        "Open requests         : " + stats.openRequests + '\n' +
        "Max open requests     : " + stats.maxOpenRequests + '\n' +
        "Total connections     : " + stats.totalConnections + '\n' +
        "Open connections      : " + stats.openConnections + '\n' +
        "Max open connections  : " + stats.maxOpenConnections + '\n' +
        "Requests timed out    : " + stats.requestTimeouts + '\n'
    }

  // pre-load models before taking requests
  TextProcessor.proc.annotate("blah")
  implicit val system = ActorSystem("parser-courier")

  import MessageProtocol._

  // needed for scheduled execution (see "in")
  implicit def executionContext = actorRefFactory.dispatcher

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    system.scheduler.scheduleOnce(duration)(body)

  val p: Int = if (args.nonEmpty) args(0).toInt else 8888
  startServer(interface = "localhost", port = p) {
    get {
      path("status") {
        complete {
          actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")
            .ask(Http.GetStats)(1.second)
            .mapTo[Stats]
        }
      }
    } ~
    post {
      path("parse") {
        entity(as[Message]) { m =>
          println(s"Received POST with text -> ${m.text}")
          val doc = TextProcessor.toAnnotatedDocument(m.text)
          // case class is implicitly converted to json
          complete(doc)
        }
      } ~
      // shuts down the server
      path("shutdown") {
        complete {
          // complete request and then shut down the server in 1 second
          in(1.second){ system.shutdown() }
          "Stopping processors-server..."
        }
      }
    }
  }
}
