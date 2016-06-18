import scala.util.matching.Regex

package object utils {

  case class RichRegex(override val regex: String) extends Regex(regex) {
    def matches(s: String) = this.pattern.matcher(s).matches
  }

  implicit def toRichRegex(regex: Regex): RichRegex = RichRegex(regex.toString)
}
