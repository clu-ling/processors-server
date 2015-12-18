name := "processors-server"

version := "1.0"

scalaVersion := "2.11.6"

// options for forked jvm
javaOptions += "-Xmx3G"

// forward sbt's stdin to forked process
connectInput in run := true

// don't show output prefix
outputStrategy := Some(StdoutOutput)

organization  := "myedibleenso.org"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  val json4sV = "3.3.0"
  Seq(
    "io.spray"            %%  "spray-can"      % sprayV,
    "io.spray"            %%  "spray-routing"  % sprayV,
    "io.spray"            %%  "spray-client"   % sprayV,
    "org.json4s"          %%  "json4s-native"  % json4sV,
    "org.json4s"          %%  "json4s-jackson" % json4sV,
    "io.spray"            %%  "spray-testkit"  % sprayV   % "test",
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV    % "test",
    "org.specs2"          %%  "specs2-core"    % "2.3.11" % "test",
    "org.clulab"          %%  "processors"     % "5.7.2",
    "org.clulab"          %%  "processors"     % "5.7.2" classifier "models"
  )
}

assemblyJarName := { s"processors-server.jar" }

assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*")}
}

assemblyMergeStrategy in assembly := {
  case "application.conf"                           => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}