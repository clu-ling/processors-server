package processors.api

import org.json4s.JValue
import processors.api


//
// Used for "annotate" and "sentiment" requests
//

trait Text {
  val text: String
}

trait Message

case class TextMessage(text: String) extends Message with Text

case class SentencesMessage(sentences: Seq[String]) extends Message

//
// Used for Odin requests
//

trait Rules {
  val rules: String
}

trait URL {
  val url: String
}

case class TextWithRules(
  text: String,
  rules: String
) extends Message with Text with Rules

case class TextWithRulesURL(
  text: String,
  url: String
) extends Message with Text with URL

case class DocumentWithRules(
  document: JValue,
  rules: String
) extends Message with Rules

case class DocumentWithRulesURL(
  document: JValue,
  url: String
) extends Message with URL
