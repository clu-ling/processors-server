package utils

import scala.util.matching.Regex


case class RichRegex(override val regex: String) extends Regex(regex) {
  def matches(s: String): Boolean = this.pattern.matcher(s).matches
}
