package processors.api

import processors.api


trait Text {
  val text: String
}

trait Message

case class TextMessage(text: String) extends Message with Text

trait Rules {
  val rules: String
}

case class TextIEMessage(
  text: String,
  rules: String
) extends Message with Text with Rules

case class DocumentIEMessage(
  document: api.Document,
  rules: String
) extends Message with Rules