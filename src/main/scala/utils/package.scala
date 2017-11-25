import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.matching.Regex


package object utils {

  implicit def toRichRegex(regex: Regex): RichRegex = RichRegex(regex.toString)

  def projectVersion: String = processors.api.BuildInfo.version
  def commit: String = {
    val c = processors.api.BuildInfo.gitHeadCommit
    val state = if (processors.api.BuildInfo.gitUncommittedChanges) "-DIRTY" else ""
    s"$c$state"
  }

  def mkDescription: Description = Description(projectVersion)

  @throws(classOf[Exception])
  @tailrec
  def buildArgMap(map: Map[String, String], args: List[String]): Map[String, String] = args match {
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
