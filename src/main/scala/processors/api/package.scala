package processors

import org.json4s.JsonDSL._
import org.json4s.{ DefaultFormats, JValue }


package object api {

  implicit val formats = DefaultFormats

  val jsonBuildInfo: JValue = {
    ("name" -> api.BuildInfo.name) ~
    ("version" -> api.BuildInfo.version) ~
    ("scalaVersion" -> api.BuildInfo.scalaVersion) ~
    ("sbtVersion" -> api.BuildInfo.sbtVersion) ~
    ("libraryDependencies" -> api.BuildInfo.libraryDependencies.toList) ~
    ("scalacOptions" -> api.BuildInfo.scalacOptions.toList) ~
    ("gitCurrentBranch" -> api.BuildInfo.gitCurrentBranch) ~
    ("gitHeadCommit" -> api.BuildInfo.gitHeadCommit) ~
    ("gitHeadCommitDate" -> api.BuildInfo.gitHeadCommitDate) ~
    ("gitUncommittedChanges" -> api.BuildInfo.gitUncommittedChanges)
  }

}
