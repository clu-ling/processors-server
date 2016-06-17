package processors.api


// storage class (mirrors the Document class in Processors) for the annotated text.
// this will be dumped to json
case class Document(
  text: String,
  sentences: Seq[Sentence]
)
