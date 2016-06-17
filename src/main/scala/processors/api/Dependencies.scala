package processors.api


case class Dependencies(
  edges: Seq[Edge],
  roots: Set[Int]
)
