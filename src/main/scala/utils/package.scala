import scala.annotation.tailrec
import scala.util.matching.Regex


package object utils {

  /** for keeping track of information related to the server **/
  case class Description(version: String)

  case class RichRegex(override val regex: String) extends Regex(regex) {
    def matches(s: String) = this.pattern.matcher(s).matches
  }

  implicit def toRichRegex(regex: Regex): RichRegex = RichRegex(regex.toString)

  def projectVersion: String = getClass.getPackage.getImplementationVersion

  def mkDescription: Description = Description(projectVersion)

  @throws(classOf[Exception])
  @tailrec
  def buildArgMap(map : Map[String, String], args: List[String]): Map[String, String] = args match {
    case Nil => map
    // handle port
    case "--port" :: port :: tail =>
      buildArgMap(map ++ Map("port" -> port), tail)
    case "-p" :: port :: tail =>
      buildArgMap(map ++ Map("port" -> port), tail)
    // handle host
    case "--host" :: host :: tail =>
      buildArgMap(map ++ Map("host" -> host), tail)
    case unknown :: tail =>
      throw new Exception(s"""Unknown option "$unknown"""")
  }
}
