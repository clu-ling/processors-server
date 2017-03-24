package service

import java.io.File
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import processors.{ConverterUtils, ProcessorsBridge, api}
import org.clulab.processors
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.{JNothing, JString}
import org.json4s.{DefaultFormats, Formats, JValue, native}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.Config


trait Service extends Json4sSupport {

  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  implicit val system: ActorSystem

  implicit def executionContext: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    system.scheduler.scheduleOnce(duration)(body)

//  def apiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(apiConnectionFlow).runWith(Sink.head)

  val routes = {
    // log results
    logRequestResult("processors-server-microservice") {
      // index page
      path("") {
        getFromResource("static/index.html")
      } ~
      // Display version
      path("version") {
        get {
          val html =
            <html>
              <body>
                <h1><code>processors-server</code> version {utils.projectVersion}</h1>
              </body>
            </html>
          complete(html)
        }
      } ~
      // Demo
      path("odin" / "demo") {
        getFromResource("static/odin.html")
      } ~
      path("images" / ".*".r) { imageResource: String =>
        val imagePath = s"static/images/$imageResource"
        println(imagePath)
        getFromResource(imagePath)
      } ~
      post {
        // display version
        path("version") {
            complete(utils.mkDescription)
        } ~
        // Handle parsing, etc
        path("api" / "annotate") {
          entity(as[api.TextMessage]) { m =>
            logger.info(s"Default Processor received POST with text -> ${m.text}")
            val processorsDoc = ProcessorsBridge.annotate(m.text)
            val json = ConverterUtils.toJSON(processorsDoc)
            complete(json)
          }
        } ~
          // annotate from sentences
          path("api" / "annotate") {
            entity(as[api.SentencesMessage]) { sm =>
              logger.info(s"Default Processor received POST with text already split into sentences...")
              val processorsDoc = ProcessorsBridge.annotate(sm.sentences)
              val json = ConverterUtils.toJSON(processorsDoc)
              complete(json)
            }
          } ~
          path("api" / "fastnlp" / "annotate") {
            entity(as[api.TextMessage]) { m =>
              logger.info(s"FastNLPProcessor received POST with text -> ${m.text}")
              val processorsDoc = ProcessorsBridge.annotateWithFastNLP(m.text)
              val json = ConverterUtils.toJSON(processorsDoc)
              complete(json)
            }
          } ~
          // annotate from sentences
          path("api" / "fastnlp" / "annotate") {
            entity(as[api.SentencesMessage]) { sm =>
              logger.info(s"FastNLPProcessor received POST with text already segmented into sentences ")
              val processorsDoc = ProcessorsBridge.annotateWithFastNLP(sm.sentences)
              val json = ConverterUtils.toJSON(processorsDoc)
              complete(json)
            }
          } ~
          path("api" / "bionlp" / "annotate") {
            entity(as[api.TextMessage]) { m =>
              logger.info(s"BioNLPProcessor received POST with text -> ${m.text}")
              val processorsDoc = ProcessorsBridge.annotateWithBioNLP(m.text)
              val json = ConverterUtils.toJSON(processorsDoc)
              complete(json)
            }
          } ~
          // annotate from sentences
          path("api" / "bionlp" / "annotate") {
            entity(as[api.SentencesMessage]) { sm =>
              logger.info(s"BioNLPProcessor received POST with text already segmented into sentences ")
              val processorsDoc = ProcessorsBridge.annotateWithBioNLP(sm.sentences)
              val json = ConverterUtils.toJSON(processorsDoc)
              complete(json)
            }
          } ~
          // Handle sentiment analysis of text
          path("api" / "sentiment" / "corenlp" / "score") {
            entity(as[api.TextMessage]) { m =>
              logger.info(s"CoreNLPSentimentAnalyzer received POST with text -> ${m.text}")
              val scores = ProcessorsBridge.toSentimentScores(m.text)
              complete(scores)
            }
          } ~
          path("api" / "sentiment" / "corenlp" / "score") {
            entity(as[JValue]) {
              // Handle sentiment analysis of a Sentence
              case s: JValue if s \ "words" != JNothing =>
                val sentence = ConverterUtils.toProcessorsSentence(s)
                val scores = ProcessorsBridge.toSentimentScores(sentence)
                complete(scores)
              // Handle sentiment analysis of a Document
              case d: JValue if d \ "sentences" != JNothing =>
                val document = ConverterUtils.toProcessorsDocument(d)
                val scores = ProcessorsBridge.toSentimentScores(document)
                complete(scores)
            }
          } ~
          // Handle sentiment analysis of a seq of text
          path("api" / "sentiment" / "corenlp" / "score" / "segmented") {
            entity(as[api.SentencesMessage]) { sm =>
              logger.info(s"CoreNLPSentimentAnalyzer received POST with text already segmented into sentences")
              val sentences: Seq[String] = sm.sentences
              val scores = ProcessorsBridge.toSentimentScores(sentences)
              complete(scores)
            }
          } ~
          path("api" / "sentiment" / "corenlp" / "score") {
            entity(as[JValue]) { json =>
              val document: processors.Document = ConverterUtils.toProcessorsDocument(json)
              val scores = ProcessorsBridge.toSentimentScores(document)
              complete(scores)
            }
          } ~
          // Handle IE with Odin
          path("api" / "odin" / "extract") {
            entity(as[api.DocumentWithRules]) { dwr =>
              logger.info(s"Odin endpoint received DocumentWithRules")
              val document = ConverterUtils.toProcessorsDocument(dwr.document)
              val mentions = ProcessorsBridge.getMentions(document, dwr.rules)
              val json = ConverterUtils.toJSON(mentions)
              complete(json)
            }
          } ~
          path("api" / "odin" / "extract") {
            entity(as[api.DocumentWithRulesURL]) { dwu =>
              logger.info(s"Odin endpoint received DocumentWithRulesURL")
              val document = ConverterUtils.toProcessorsDocument(dwu.document)
              val mentions = ProcessorsBridge.getMentions(document, ConverterUtils.urlToRules(dwu.url))
              val json = ConverterUtils.toJSON(mentions)
              complete(json)
            }
          } ~
          path("api" / "odin" / "extract") {
            entity(as[api.TextWithRules]) { twr =>
              logger.info(s"Odin endpoint received TextWithRules")
              val document = ProcessorsBridge.annotateWithFastNLP(twr.text)
              val mentions = ProcessorsBridge.getMentions(document, twr.rules)
              val json = ConverterUtils.toJSON(mentions)
              complete(json)
            }
          } ~
          path("api" / "odin" / "extract") {
            entity(as[api.TextWithRulesURL]) { twu =>
              logger.info(s"Odin endpoint received TextWithRulesURL")
              val document = ProcessorsBridge.annotateWithFastNLP(twu.text)
              val mentions = ProcessorsBridge.getMentions(document, ConverterUtils.urlToRules(twu.url))
              val json = ConverterUtils.toJSON(mentions)
              complete(json)
            }
          } ~
          // shuts down the server
          path("shutdown") {
            complete {
              // complete request and then shut down the server in 1 second
              in(1.second) {
                system.terminate()
              }
              "Stopping processors-server..."
            }
          }
      }
    }
  }
}

