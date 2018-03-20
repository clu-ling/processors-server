package service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import processors.{ api, ConverterUtils, ProcessorsBridge }
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.{ jackson, DefaultFormats, JNothing, JValue }

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContextExecutor, Future }
import com.typesafe.config.Config

trait Service {

  implicit val htmlMarshaller = ScalaXmlSupport.nodeSeqMarshaller(MediaTypes.`text/html`)
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  implicit val system: ActorSystem

  implicit def executionContext: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    system.scheduler.scheduleOnce(duration)(body)

  //  def apiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(apiConnectionFlow).runWith(Sink.head)

  def route(implicit materializer: Materializer) = {
    // log results
    logRequestResult("processors-server-microservice") {
      // index page
      path("") {
        getFromResource("static/index.html")
      } ~
        // Display version
        path("version") {
          get {
            val html = Future {
              <html>
                <body>
                  <h1>
                    <code>processors-server</code>
                    version
                    {utils.projectVersion}
                    (
                    {utils.commit}
                    )</h1>
                </body>
              </html>
            }
            complete(html)
          }
        } ~
        // buildInfo
        path("buildinfo") {
          get {
            val resp = Future { api.jsonBuildInfo }
            complete(resp)
          }
        } ~
        // Demos
        path("nlp" / "demo") {
          getFromResource("static/annotation-demo.html")
        } ~
        path("odin" / "demo") {
          getFromResource("static/odin.html")
        } ~
        // resources
        path("favicon.ico") {
          val resourcePath = s"static/images/favicon.ico"
          getFromResource(resourcePath)
        } ~
        path("js" / ".*".r) { resource: String =>
          val resourcePath = s"static/js/$resource"
          logger.debug(resource)
          getFromResource(resourcePath)
        } ~
        path("css" / ".*".r) { resource: String =>
          val resourcePath = s"static/css/$resource"
          logger.debug(resource)
          getFromResource(resourcePath)
        } ~
        path("fonts" / ".*".r) { resource: String =>
          val resourcePath = s"static/fonts/$resource"
          logger.debug(resource)
          getFromResource(resourcePath)
        } ~
        path("images" / ".*".r) { imageResource: String =>
          val imagePath = s"static/images/$imageResource"
          logger.debug(imagePath)
          getFromResource(imagePath)
        } ~
        post {
          // display version
          path("version") {
            val resp = Future { utils.mkDescription }
            complete(resp)
          } ~
            // Handle parsing, etc
            path("api" / "annotate") {
              entity(as[api.TextMessage]) { m =>
                val resp = Future {
                  logger.info(s"Default Processor received POST with text -> ${m.text}")
                  val processorsDoc = ProcessorsBridge.annotate(m.text)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            // annotate from sentences
            path("api" / "annotate") {
              entity(as[api.SegmentedMessage]) { sm =>
                val resp = Future {
                  logger.info(s"Default Processor received POST with text already split into sentences...")
                  val processorsDoc = ProcessorsBridge.annotate(sm.segments)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            path("api" / "clu" / "annotate") {
              entity(as[api.TextMessage]) { m =>
                val resp = Future {
                  logger.info(s"CluProcessor received POST with text -> ${m.text}")
                  val processorsDoc = ProcessorsBridge.annotateWithClu(m.text)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            path("api" / "clu" / "stanford" / "annotate") {
              entity(as[api.TextMessage]) { m =>
                val resp = Future {
                  logger.info(s"CluProcessorWithStanford received POST with text -> ${m.text}")
                  val processorsDoc = ProcessorsBridge.annotateWithCluStanford(m.text)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            path("api" / "clu" / "stanford" / "annotate") {
              entity(as[api.SegmentedMessage]) { sm =>
                val resp = Future {
                  logger.info(s"CluProcessorWithStanford received POST with text already segmented into sentences ")
                  val processorsDoc = ProcessorsBridge.annotateWithCluStanford(sm.segments)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            path("api" / "clu" / "bio" / "annotate") {
              entity(as[api.TextMessage]) { m =>
                val resp = Future {
                  logger.info(s"BioCluProcessor received POST with text -> ${m.text}")
                  val processorsDoc = ProcessorsBridge.annotateWithCluBio(m.text)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            // annotate from sentences
            path("api" / "clu" / "bio" / "annotate") {
              entity(as[api.SegmentedMessage]) { sm =>
                val resp = Future {
                  logger.info(s"BioCluProcessor received POST with text already segmented into sentences ")
                  val processorsDoc = ProcessorsBridge.annotateWithCluBio(sm.segments)
                  ConverterUtils.toJSON(processorsDoc)
                }
                complete(resp)
              }
            } ~
            // lemmatize
            path("api" / "clu" / "lemmatize") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu lemmatizer")
                    val lemmatizedSentence = ProcessorsBridge.lemmatize(sentence)
                    ConverterUtils.toJSON(lemmatizedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu lemmatizer")
                    val lemmatizedDoc = ProcessorsBridge.lemmatize(document)
                    ConverterUtils.toJSON(lemmatizedDoc)
                  }
                  complete(resp)

              }
            } ~
            // PoS tag
            path("api" / "clu" / "tag-parts-of-speech") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu PoS tagger")
                    val taggedSentence = ProcessorsBridge.tag(sentence)
                    ConverterUtils.toJSON(taggedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu PoS tagger")
                    val taggedDoc = ProcessorsBridge.tag(document)
                    ConverterUtils.toJSON(taggedDoc)
                  }
                  complete(resp)

              }
            } ~
            // chunk
            path("api" / "clu" / "chunk") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu chunker")
                    val chunkedSentence = ProcessorsBridge.chunk(sentence)
                    ConverterUtils.toJSON(chunkedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu chunker")
                    val chunkedDoc = ProcessorsBridge.chunk(document)
                    ConverterUtils.toJSON(chunkedDoc)
                  }
                  complete(resp)

              }
            } ~
            // NER
            path("api" / "clu" / "ner") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu ner")
                    val chunkedSentence = ProcessorsBridge.ner(sentence)
                    ConverterUtils.toJSON(chunkedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu ner")
                    val chunkedDoc = ProcessorsBridge.ner(document)
                    ConverterUtils.toJSON(chunkedDoc)
                  }
                  complete(resp)

              }
            } ~
            // dependency parsing (universal deps)
            path("api" / "clu" / "parse" / "universal") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu ner")
                    val chunkedSentence = ProcessorsBridge.parseUniversal(sentence)
                    ConverterUtils.toJSON(chunkedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu ner")
                    val chunkedDoc = ProcessorsBridge.parseUniversal(document)
                    ConverterUtils.toJSON(chunkedDoc)
                  }
                  complete(resp)

              }
            } ~
            // dependency parsing (Stanford deps)
            path("api" / "clu" / "parse" / "stanford") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Clu ner")
                    val chunkedSentence = ProcessorsBridge.parseStanford(sentence)
                    ConverterUtils.toJSON(chunkedSentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Clu ner")
                    val chunkedDoc = ProcessorsBridge.parseStanford(document)
                    ConverterUtils.toJSON(chunkedDoc)
                  }
                  complete(resp)

              }
            } ~
            // Handle IE with Odin
            path("api" / "odin" / "extract") {
              entity(as[JValue]) {

                case dwu if dwu \ "document" != JNothing && dwu \ "url" != JNothing =>
                  val resp = Future {
                    logger.info(s"Odin endpoint received DocumentWithRulesURL")
                    val document = ConverterUtils.toProcessorsDocument(dwu \ "document")
                    val url = (dwu \ "url").extract[String]
                    ProcessorsBridge.getMentionsAsJSON(document, ConverterUtils.urlToRules(url))
                  }
                  complete(resp)

                case dwr if dwr \ "document" != JNothing && dwr \ "rules" != JNothing =>
                  val resp = Future {
                    logger.info(s"Odin endpoint received DocumentWithRules")
                    val document = ConverterUtils.toProcessorsDocument(dwr \ "document")
                    val rules = (dwr \ "rules").extract[String]
                    ProcessorsBridge.getMentionsAsJSON(document, rules)
                  }
                  complete(resp)

                case twr if twr \ "text" != JNothing && twr \ "rules" != JNothing =>
                  val resp = Future {
                    logger.info(s"Odin endpoint received TextWithRules")
                    val text = (twr \ "text").extract[String]
                    val rules = (twr \ "rules").extract[String]
                    val document = ProcessorsBridge.annotate(text)
                    ProcessorsBridge.getMentionsAsJSON(document, rules)
                  }
                  complete(resp)

                case twu if twu \ "text" != JNothing && twu \ "url" != JNothing =>
                  val resp = Future {
                    logger.info(s"Odin endpoint received TextWithRulesURL")
                    val text = (twu \ "text").extract[String]
                    val url = (twu \ "url").extract[String]
                    val document = ProcessorsBridge.annotate(text)
                    ProcessorsBridge.getMentionsAsJSON(document, ConverterUtils.urlToRules(url))
                  }
                  complete(resp)

              }
            } ~
            path("api" / "openie" / "entities" / "extract") {
              entity(as[JValue]) {
                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractEntities(sentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractEntities(document)
                  }
                  complete(resp)

              }
            } ~
            path("api" / "openie" / "entities" / "base-extract") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractBaseEntities(sentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractBaseEntities(document)
                  }
                  complete(resp)

              }
            } ~
            path("api" / "openie" / "entities" / "extract-filter") {
              entity(as[JValue]) {

                case s: JValue if s \ "words" != JNothing =>
                  val resp = Future {
                    val sentence = ConverterUtils.toProcessorsSentence(s)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractAndFilterEntities(sentence)
                  }
                  complete(resp)

                case d: JValue if d \ "sentences" != JNothing =>
                  val resp = Future {
                    val document = ConverterUtils.toProcessorsDocument(d)
                    logger.info(s"Openie Entity Extractor")
                    ProcessorsBridge.extractAndFilterEntities(document)
                  }
                  complete(resp)

              }
            } //~
//            // shuts down the server
//            path("shutdown") {
//              complete {
//                // complete request and then shut down the server in 1 second
//                in(1.second) {
//                  system.terminate()
//                }
//                "Stopping processors-server..."
//              }
//            }
        }
    }
  }
}
