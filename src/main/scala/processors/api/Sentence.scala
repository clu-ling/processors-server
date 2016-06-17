package processors.api


case class Sentence(
  words: Seq[String],
  startOffsets: Seq[Int],
  endOffsets: Seq[Int],
  lemmas: Seq[String],
  tags: Seq[String],
  entities: Seq[String],
  dependencies: Dependencies
)
