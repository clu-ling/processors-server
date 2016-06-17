import com.typesafe.scalalogging.LazyLogging
import org.json4s.{Formats, DefaultFormats}
import processors.api
import spray.http.ContentTypes
import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import scala.util.{Failure, Success}
import processors._
import scala.concurrent.duration._
import akka.pattern.ask
import spray.can.server.Stats
import spray.can.Http
import spray.httpx.marshalling.Marshaller
import spray.httpx.Json4sSupport
import spray.util._

// for handling incoming json
object MessageProtocol extends Json4sSupport {

  override implicit def json4sFormats: Formats = DefaultFormats

}

/**
 * Spray server for NLP (bridge to processors library)
 */
object NLPServer extends App with SimpleRoutingApp with LazyLogging {

  val defaultPort = 8888

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
  ProcessorsBridge.annotateWithFastNLP("blah")
  ProcessorsBridge.annotateWithBioNLP("blah")

  implicit val system = ActorSystem("processors-courier")

  import MessageProtocol._

  // needed for scheduled execution (see "in")
  implicit def executionContext = actorRefFactory.dispatcher

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    system.scheduler.scheduleOnce(duration)(body)

  val p: Int = if (args.nonEmpty) args(0).toInt else defaultPort
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
      // Handle parsing, etc
      path("annotate") {
        entity(as[api.TextMessage]) { m =>
          logger.info(s"Default Processor received POST with text -> ${m.text}")
          val processorsDoc = ProcessorsBridge.annotate(m.text)
          val doc = ConverterUtils.toDocument(processorsDoc)
          complete(doc)
        }
      } ~
      path("fastnlp" / "annotate") {
        entity(as[api.TextMessage]) { m =>
          logger.info(s"FastNLPProcessor received POST with text -> ${m.text}")
          val processorsDoc = ProcessorsBridge.annotateWithFastNLP(m.text)
          val doc = ConverterUtils.toDocument(processorsDoc)
          complete(doc)
        }
      } ~
      path("bionlp" / "annotate") {
        entity(as[api.TextMessage]) { m =>
          logger.info(s"BioNLPProcessor received POST with text -> ${m.text}")
          val processorsDoc = ProcessorsBridge.annotateWithBioNLP(m.text)
          val doc = ConverterUtils.toDocument(processorsDoc)
          complete(doc)
        }
      } ~
      // Handle sentiment analysis of text
      path("corenlp" / "sentiment" / "text") {
        entity(as[api.TextMessage]) { m =>
          logger.info(s"CoreNLPSentimentAnalyzer received POST with text -> ${m.text}")
          val scores = ProcessorsBridge.toSentimentScores(m.text)
          complete(scores)
        }
      } ~
      // Handle sentiment analysis of a Sentence
      path("corenlp" / "sentiment" / "sentence") {
        entity(as[api.Sentence]) { s =>
          logger.info(s"CoreNLPSentimentAnalyzer received Sentence in POST with text -> ${s.words.mkString(" ")}")
          val sentence = ConverterUtils.toProcessorsSentence(s)
          val scores = ProcessorsBridge.toSentimentScores(sentence)
          complete(scores)
        }
      } ~
      // Handle sentiment analysis of a Document
      path("corenlp" / "sentiment" / "document") {
        entity(as[api.Document]) { doc =>
          logger.info(s"CoreNLPSentimentAnalyzer received Document in POST with text -> ${doc.text}")
          val document = ConverterUtils.toProcessorsDocument(doc)
          val scores = ProcessorsBridge.toSentimentScores(document)
          complete(scores)
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
