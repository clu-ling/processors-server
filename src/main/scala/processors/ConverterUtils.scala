package processors

import com.typesafe.scalalogging.LazyLogging
import org.clulab.odin.{EventMention, Mention, RelationMention, TextBoundMention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.serialization.json.JSONSerializer
import org.clulab.serialization.json._
import org.json4s.JValue

import scala.io.Source
import utils._


object ConverterUtils extends LazyLogging {

  // For validating URLs to rule files
  // ex. https://raw.githubusercontent.com/clulab/reach/508697db2217ba14cd1fa0a99174816cc3383317/src/main/resources/edu/arizona/sista/demo/open/grammars/rules.yml
  val rulesURL = RichRegex("""(https?|ftp).+?\.(yml|yaml)$""")

  @throws(classOf[api.BadURLException])
  def urlToRules(url: String): String = rulesURL.matches(url) match {
    case true =>
      logger.info(s"Retrieving Odin rules from $url")
      val page = Source.fromURL(url)
      val rules = page.mkString
      rules
    case false => throw new api.BadURLException(url)
  }

  def toProcessorsSentence(json: JValue): Sentence = JSONSerializer.toSentence(json)

  def toProcessorsDocument(json: JValue): Document = JSONSerializer.toDocument(json)

  def toJSON(document: Document): JValue = document.jsonAST

  def toJSON(mentions: Seq[Mention]): JValue = mentions.jsonAST

}