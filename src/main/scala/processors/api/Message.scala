package processors.api

//
// Used for "annotate" and "sentiment" requests
//

trait Text {
  val text: String
}

trait Message

case class TextMessage(text: String) extends Message with Text

/**
  * Container for text that has already been split into sentences.
  * `segments` is used instead of `sentences` to avoid ambiguities with [[org.clulab.processors.Document]] json i
  */
case class SegmentedMessage(segments: Seq[String]) extends Message
