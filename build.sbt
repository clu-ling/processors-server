name := "processors-server"

version := "3.0"

scalaVersion := "2.11.8"

// options for forked jvm
javaOptions += "-Xmx3G"

// forward sbt's stdin to forked process
connectInput in run := true

// don't show output prefix
outputStrategy := Some(StdoutOutput)

organization  := "myedibleenso"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

//logLevel := Level.Info

libraryDependencies ++= {
  val akkaV = "2.4.3"
  val json4sV = "3.3.0"
  val procV = "6.0.0"
  //val twirlV = "1.2.0"

  Seq(
    "com.typesafe"                        %  "config"                                % "1.3.0",
    "org.json4s"                         %%  "json4s-native"                         % json4sV,
    "org.json4s"                         %%  "json4s-jackson"                        % json4sV,
    "de.heikoseeberger"                  %%  "akka-http-json4s"                      % "1.7.0",
    // Twirl
    //"com.typesafe.play"                  %% "twirl-api"                              % twirlV,
    // AKKA
    "com.typesafe.akka"                  %%  "akka-actor"                            % akkaV,
    "com.typesafe.akka"                  %%  "akka-stream"                           % akkaV,
    "com.typesafe.akka"                  %%  "akka-http-experimental"                % akkaV,
    "com.typesafe.akka"                  %%  "akka-http-spray-json-experimental"     % akkaV,
    "com.typesafe.akka"                  %%  "akka-http-testkit"                     % akkaV,
    "com.typesafe.akka"                  %%  "akka-actor"                            % akkaV,
    "com.typesafe.akka"                  %%  "akka-testkit"                          % akkaV    % "test",
    "com.typesafe.akka"                  %%  "akka-slf4j"                            % akkaV,
    "com.typesafe.akka"                  %%  "akka-http-xml-experimental"            % akkaV,
    "org.specs2"                         %%  "specs2-core"                           % "2.3.11" % "test",
    "org.clulab"                         %%  "processors-main"                       % procV,
    "org.clulab"                         %%  "processors-corenlp"                    % procV,
    "org.clulab"                         %%  "processors-models"                     % procV,
    // logging
    "ch.qos.logback"                      %  "logback-classic"                       % "1.1.7",
    "com.typesafe.scala-logging"         %%  "scala-logging"                         % "3.4.0",
    // testing
    "org.scalatest"                      %% "scalatest"                              % "2.2.6" % Test
  )
}

assemblyJarName := { s"processors-server.jar" }

assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*")}
}

assemblyMergeStrategy in assembly := {
  case "application.conf" => MergeStrategy.concat
  // Gets rid of ${spray.version} error
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
