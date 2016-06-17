package processors.api

import processors.api


case class Mention(
  label: String,
  labels: Set[String],
  arguments: Map[String, Seq[api.Mention]],
  trigger: Option[api.Mention],
  start: Int,
  end: Int,
  sentence: Int,
  document: api.Document,
  keep: Boolean,
  foundBy: String
) {

}

object Mention {

  def apply(
    label: String,
    start: Int,
    end: Int,
    sentence: Int,
    document: api.Document,
    foundBy: String
  ) = {
    new Mention(
      label,
      Set(label),
      Map.empty[String, Seq[api.Mention]],
      None,
      start,
      end,
      sentence,
      document,
      true,
      foundBy
    )
  }
}
