package processors.api


//
// Used for "annotate" and "sentiment" requests
//

trait Text {
  val text: String
}

trait Message

case class TextMessage(text: String) extends Message with Text

case class SentencesMessage(sentences: Seq[String]) extends Message

