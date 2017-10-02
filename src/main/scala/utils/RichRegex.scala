package utils

import scala.util.matching.Regex


case class RichRegex(override val regex: String) extends Regex(regex) {
  def matches(s: String) = this.pattern.matcher(s).matches
}
