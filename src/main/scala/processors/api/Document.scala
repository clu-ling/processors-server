package processors.api

import org.json4s.JValue


// storage class (mirrors the Document class in Processors) for the annotated text.
// this will be dumped to json
case class Document(
  text: String,
  sentences: Seq[Sentence],
  discourseTree: Option[JValue] = None
)
