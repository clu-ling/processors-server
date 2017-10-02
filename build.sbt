lazy val commonScalacOptions = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  // "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-encoding", "utf8"
)

lazy val commonSettings = Seq(
  name := "processors-server",
  organization := "myedibleenso",
  scalaVersion in ThisBuild := "2.11.11", // avoid warnings when compiling play project with -Ywarn-unused
  version in ThisBuild := "3.1.0",
  // we want to use -Ywarn-unused-import most of the time
  scalacOptions ++= commonScalacOptions,
  scalacOptions += "-Ywarn-unused-import",
  // -Ywarn-unused-import is annoying in the console
  scalacOptions in (Compile, console) := commonScalacOptions,
  // show test duration
  testOptions in Test += Tests.Argument("-oD"),
  excludeDependencies += "commons-logging" % "commons-logging"
)

lazy val npmSettings = Seq(
  npmWorkingDir := "ui",
  npmCompileCommands := "run all",
  npmTestCommands := "test",
  npmCleanCommands := "run clean"
)

lazy val dockerSettings = Seq(
  dockerfile in docker := {
    val targetDir = "/app"
    // the assembly task generates a fat jar
    val artifact: File = assembly.value
    val artifactTargetPath = s"$targetDir/${artifact.name}"
    val productionConf = "production.conf"
    new Dockerfile {
      from("openjdk:8-jdk")
      add(artifact, artifactTargetPath)
      copy(new File(productionConf), file(s"$targetDir/$productionConf"))
      entryPoint("java", s"-Dconfig.file=$targetDir/$productionConf", "-jar", artifactTargetPath)
    }
  },
  imageNames in docker := Seq(
    // sets the latest tag
    ImageName(
      namespace = Some(organization.value),
      repository = name.value,
      tag = Some("latest")
    ),
    // sets a name with a tag that contains the project version
    ImageName(
      namespace = Some(organization.value),
      repository = name.value,
      tag = Some(version.value)
    )
  )
)

lazy val assemblySettings = Seq(
  assemblyJarName := { s"processors-server.jar" },
  mainClass in assembly := Some("NLPServer"),
  assemblyExcludedJars in assembly := {
    val cp = (fullClasspath in assembly).value
    cp filter { x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*") }
  },
  assemblyMergeStrategy in assembly := {
    //case c if c.endsWith("net.sf.ehcache.EhcacheInit") => MergeStrategy.first
    case netty if netty.endsWith("io.netty.versions.properties") => MergeStrategy.first
    //case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val buildInfoSettings = Seq(
  buildInfoPackage := "processors.api",
  buildInfoKeys := Seq[BuildInfoKey](
    name, version, scalaVersion, sbtVersion, libraryDependencies, scalacOptions,
    "gitCurrentBranch" -> { git.gitCurrentBranch.value },
    "gitHeadCommit" -> { git.gitHeadCommit.value.getOrElse("") },
    "gitHeadCommitDate" -> { git.gitHeadCommitDate.value.getOrElse("") },
    "gitUncommittedChanges" -> { git.gitUncommittedChanges.value }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime,
  buildInfoOptions += BuildInfoOption.ToJson
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(sbtdocker.DockerPlugin)
  .enablePlugins(Npm)
  .settings(buildInfoSettings)
  .settings(npmSettings)
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(assemblySettings)
  .settings(
    name := "processors-server",
    aggregate in test := false
  )

//logLevel := Level.Info

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= {
  val akkaV = "2.5.4"
  val akkaHTTPV = "10.0.10"
  val json4sV = "3.5.3"
  val procV = "6.1.4-SNAPSHOT"

  Seq(
    "com.typesafe"                        %  "config"                                % "1.3.0",
    "org.json4s"                         %%  "json4s-core"                           % json4sV,
    "org.json4s"                         %%  "json4s-jackson"                        % json4sV,
    "de.heikoseeberger"                  %%  "akka-http-json4s"                      % "1.17.0",
    // AKKA
    "com.typesafe.akka"                  %%  "akka-actor"                            % akkaV,
    "com.typesafe.akka"                  %%  "akka-stream"                           % akkaV,
    "com.typesafe.akka"                  %%  "akka-slf4j"                            % akkaV,
    // akka-http
    "com.typesafe.akka"                  %%  "akka-http-core"                        % akkaHTTPV,
    "com.typesafe.akka"                  %%  "akka-http"                             % akkaHTTPV,
    "com.typesafe.akka"                  %%  "akka-http-testkit"                     % akkaHTTPV,
    "com.typesafe.akka"                  %%  "akka-http-xml"                         % akkaHTTPV,
    // processors
    "org.clulab"                         %% "processors-main"                        % procV,
    "org.clulab"                         %% "processors-corenlp"                     % procV,
    "org.clulab"                         %% "processors-odin"                        % procV,
    "org.clulab"                         %% "processors-modelsmain"                  % procV,
    "org.clulab"                         %% "processors-modelscorenlp"               % procV,
    // testing
    "org.specs2"                         %%  "specs2-core"                           % "2.3.11" % "test",
    "com.typesafe.akka"                  %%  "akka-testkit"                          % akkaV    % "test",
    // logging
    "ch.qos.logback"                      %  "logback-classic"                       % "1.1.7",
    "com.typesafe.scala-logging"         %%  "scala-logging"                         % "3.4.0",
    // testing
    "org.scalatest"                      %% "scalatest"                              % "2.2.6" % Test
  )
}


