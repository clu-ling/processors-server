
lazy val commonScalacOptions = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  // "-Ywarn-value-discard",
  "-Ywarn-unused",
  // format: off
  "-encoding", "utf8"
  // format: on
)

lazy val commonSettings = Seq(
  name := "processors-server",
  organization := "parsertongue",
  scalaVersion := "2.11.11",
  //scalaVersion := "2.12.4",
  //crossScalaVersions := Seq("2.11.11", "2.12.4"),
  // we want to use -Ywarn-unused-import most of the time
  scalacOptions ++= commonScalacOptions,
  scalacOptions += "-Ywarn-unused-import",
  // -Ywarn-unused-import is annoying in the console
  scalacOptions in (Compile, console) := commonScalacOptions,
  // show test duration
  testOptions in Test += Tests.Argument("-oD"),
  excludeDependencies += "commons-logging" % "commons-logging"
)

lazy val dockerSettings = Seq(
  dockerfile in docker := {
    val targetDir = "/app"
    // the assembly task generates a fat jar
    val artifact: File = assembly.value
    val artifactTargetPath = s"$targetDir/${artifact.name}"
    val productionConf = "production.conf"
    new Dockerfile {
      //from("openjdk:8-jdk")
      from("openjdk")
      add(artifact, artifactTargetPath)
      copy(new File(productionConf), file(s"$targetDir/$productionConf"))
      entryPoint("java", s"-Dconfig.file=$targetDir/$productionConf", "-jar", artifactTargetPath)
    }
  },
  imageNames in docker := {
    val commit = git.gitHeadCommit.value.getOrElse(s"v${version.value}")
    Seq(
      // sets the latest tag
      ImageName(s"${organization.value}/${name.value}:latest"),
      // use git hash
      ImageName(s"${organization.value}/${name.value}:${commit}"),
      // use processors version
	  ImageName(s"${organization.value}/${name.value}:processors-${procV}"),
      // sets a name with a tag that contains the project version
      ImageName(
        namespace = Some(organization.value),
        repository = name.value,
        tag = Some("v" + version.value)
      )
    )
    }  
  )
lazy val assemblySettings = Seq(
  assemblyJarName := { s"processors-server.jar" },
  mainClass in assembly := Some("NLPServer")
)

lazy val buildInfoSettings = Seq(
  buildInfoPackage := "processors.api",
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    libraryDependencies,
    scalacOptions,
    "gitCurrentBranch" -> { git.gitCurrentBranch.value },
    "gitHeadCommit" -> { git.gitHeadCommit.value.getOrElse("") },
    "gitHeadCommitDate" -> { git.gitHeadCommitDate.value.getOrElse("") },
    "gitUncommittedChanges" -> { git.gitUncommittedChanges.value }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime,
  buildInfoOptions += BuildInfoOption.ToJson
)

// check style per scalastyle-config.xml
lazy val scalaStyleSettings = Seq(
  scalastyleFailOnError := true,
  scalastyleFailOnWarning := false
)

// format code per .scalfmt.conf
lazy val scalaFormattingSettings = Seq(
  scalafmtShowDiff in (ThisBuild, scalafmt) := true,
  // run BEFORE compile, etc.
  scalafmtOnCompile in ThisBuild := true,
  scalafmtTestOnCompile in ThisBuild := true
)

lazy val testScalastyle = taskKey[Unit]("run scalastyle for tests")
testScalastyle := scalastyle.in(Test).toTask("").value

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(sbtdocker.DockerPlugin)
  .enablePlugins(ScalastylePlugin)
  .settings(buildInfoSettings)
  .settings(commonSettings)
  .settings(dockerSettings)
  .settings(assemblySettings)
  .settings(scalaStyleSettings)
  .settings(scalaFormattingSettings)
  .settings(
    name := "processors-server",
    aggregate in test := false,
    (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
    (test in Test) := ((test in Test) dependsOn testScalastyle).value
  )

//logLevel := Level.Info

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val procV = "6.3.0"

libraryDependencies ++= {
  val akkaV = "2.5.9"
  val akkaHTTPV = "10.1.0-RC2"
  val json4sV = "3.5.3"

  Seq(
    "com.typesafe"      % "config"            % "1.3.0",
    "org.json4s"        %% "json4s-core"      % json4sV,
    "org.json4s"        %% "json4s-jackson"   % json4sV,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.20.0-RC2",
    // AKKA
    "com.typesafe.akka" %% "akka-actor"  % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"  % akkaV,
    // akka-http
    "com.typesafe.akka" %% "akka-http-core"    % akkaHTTPV,
    "com.typesafe.akka" %% "akka-http"         % akkaHTTPV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHTTPV,
    "com.typesafe.akka" %% "akka-http-xml"     % akkaHTTPV,
    // processors
    "org.clulab" %% "processors-main"          % procV,
    "org.clulab" %% "processors-corenlp"       % procV,
    "org.clulab" %% "processors-odin"          % procV,
    "org.clulab" %% "processors-openie"          % procV,
    "org.clulab" %% "processors-modelsmain"    % procV,
    "org.clulab" %% "processors-modelscorenlp" % procV,
    // testing
    "org.specs2"        %% "specs2-core"  % "2.3.11" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaV    % "test",
    // logging
    "ch.qos.logback"             % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging"  % "3.4.0",
    // testing
    "org.scalatest" %% "scalatest" % "2.2.6" % Test
  )
}

addCommandAlias("dockerize", ";clean;compile;test;buildFrontend;docker")
addCommandAlias("dockerizeWebappAndPushToDockerHub", ";clean;compile;test;buildFrontend;docker;dockerPush")
addCommandAlias("jarify", ";clean;compile;test;buildFrontend;assembly")

lazy val buildFrontend = taskKey[Unit]("Execute frontend scripts")
buildFrontend := {
  val s: TaskStreams = streams.value
  val shell: Seq[String] = Seq("bash", "-c")
  val npmInstall: Seq[String] = shell :+ "(cd ui && npm install --no-optional)" // avoid contextify error
  val npmTasks: Seq[String] = shell :+   "(cd ui && npm run all)"
  s.log.info("building frontend...")
  if((npmInstall #&& npmTasks !) == 0) {
    s.log.success("frontend build successful!")
  } else {
    throw new IllegalStateException("frontend build failed!")
  }
}
