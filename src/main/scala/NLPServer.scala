import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{Formats, DefaultFormats}
import processors.api
import spray.http.ContentTypes
import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import processors._
import utils.buildArgMap
import org.clulab.processors
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

  // determine server configuration
  // mostly useful for passing args to fat jar
  val argMap = buildArgMap(ServerConfig.defaults, args.toList)
  val p: Int = ServerConfig.defaults(ServerConfig.port).toInt
  val h: String = ServerConfig.defaults(ServerConfig.host)

  startServer(interface = h, port = p) {
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
      // annotate from sentences
      path("annotate") {
        entity(as[api.SentencesMessage]) { sm =>
          logger.info(s"Default Processor received POST with text already split into sentences...")
          val processorsDoc = ProcessorsBridge.annotate(sm.sentences)
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
      // annotate from sentences
      path("fastnlp" / "annotate") {
        entity(as[api.SentencesMessage]) { sm =>
          logger.info(s"FastNLPProcessor received POST with text already segmented into sentences ")
          val processorsDoc = ProcessorsBridge.annotateWithFastNLP(sm.sentences)
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
      // annotate from sentences
      path("bionlp" / "annotate") {
        entity(as[api.SentencesMessage]) { sm =>
          logger.info(s"BioNLPProcessor received POST with text already segmented into sentences ")
          val processorsDoc = ProcessorsBridge.annotateWithBioNLP(sm.sentences)
          val doc = ConverterUtils.toDocument(processorsDoc)
          complete(doc)
        }
      } ~
      // Handle sentiment analysis of text
      path("sentiment" / "corenlp" / "score") {
        entity(as[api.TextMessage]) { m =>
          logger.info(s"CoreNLPSentimentAnalyzer received POST with text -> ${m.text}")
          val scores = ProcessorsBridge.toSentimentScores(m.text)
          complete(scores)
        }
      } ~
      // Handle sentiment analysis of a Sentence
      path("sentiment" / "corenlp" / "score") {
        entity(as[api.Sentence]) { s =>
          logger.info(s"CoreNLPSentimentAnalyzer received Sentence in POST with text -> ${s.words.mkString(" ")}")
          val sentence: processors.Sentence = ConverterUtils.toProcessorsSentence(s)
          val scores = ProcessorsBridge.toSentimentScores(sentence)
          complete(scores)
        }
      } ~
      // Handle sentiment analysis of a seq of text
      path("sentiment" / "corenlp" / "score" / "segmented") {
        entity(as[api.SentencesMessage]) { sm =>
          logger.info(s"CoreNLPSentimentAnalyzer received POST with text already segmented into sentences")
          val sentences: Seq[String] = sm.sentences
          val scores = ProcessorsBridge.toSentimentScores(sentences)
          complete(scores)
        }
      } ~
      // Handle sentiment analysis of a Document
      path("sentiment" / "corenlp" / "score") {
        entity(as[api.Document]) { doc =>
          logger.info(s"CoreNLPSentimentAnalyzer received Document in POST with text -> ${doc.text}")
          val document: processors.Document = ConverterUtils.toProcessorsDocument(doc)
          val scores = ProcessorsBridge.toSentimentScores(document)
          complete(scores)
        }
      } ~
      // Handle IE with Odin
      path("odin" / "extract") {
        entity(as[api.DocumentWithRules]) { dwr =>
          logger.info(s"Odin endpoint received DocumentWithRules")
          val document = ConverterUtils.toProcessorsDocument(dwr.document)
          val mentions = ProcessorsBridge.getMentions(document, dwr.rules)
          complete(mentions)
        }
      } ~
      path("odin" / "extract") {
        entity(as[api.DocumentWithRulesURL]) { dwu =>
          logger.info(s"Odin endpoint received DocumentWithRulesURL")
          val document = ConverterUtils.toProcessorsDocument(dwu.document)
          val mentions = ProcessorsBridge.getMentions(document, ConverterUtils.urlToRules(dwu.url))
          complete(mentions)
        }
      } ~
      path("odin" / "extract") {
        entity(as[api.TextWithRules]) { twr =>
          logger.info(s"Odin endpoint received TextWithRules")
          val document = ProcessorsBridge.annotateWithFastNLP(twr.text)
          val mentions = ProcessorsBridge.getMentions(document, twr.rules)
          complete(mentions)
        }
      } ~
      path("odin" / "extract") {
        entity(as[api.TextWithRulesURL]) { twu =>
          logger.info(s"Odin endpoint received TextWithRulesURL")
          val document = ProcessorsBridge.annotateWithFastNLP(twu.text)
          val mentions = ProcessorsBridge.getMentions(document, ConverterUtils.urlToRules(twu.url))
          complete(mentions)
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

/**
 * Server configuration
 */
object ServerConfig {

  val config = ConfigFactory.load()
  val port = "port"
  val host = "host"
  val defaultPort = config.getString("defaults.port")
  val defaultHostName = config.getString("defaults.host")
  val defaults = Map(
    port -> defaultPort,
    host -> defaultHostName
  )
}
