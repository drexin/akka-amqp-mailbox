import sbt._
import sbt.Keys._
import com.typesafe.sbtscalariform.ScalariformPlugin
import com.typesafe.sbtscalariform.ScalariformPlugin.ScalariformKeys

object AkkaAmqpMailboxBuild extends Build {

  lazy val root = Project(
    id = "akka-amqp-mailbox",
    base = file("."),
    settings = defaultSettings ++ Seq(
      parallelExecution in GlobalScope := System.getProperty("akka.parallelExecution", "false").toBoolean,
      libraryDependencies ++= Dependencies.root
    )
  )

  override lazy val settings = super.settings ++ buildSettings ++ resolverSettings

  lazy val resolverSettings = Seq(
    resolvers ++= Seq(
      "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
    )
  )

  lazy val buildSettings = Seq(
    organization := "com.typesafe.akka",
    version := "2.0.1",
    scalaVersion := "2.9.2"
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ formatSettings ++ Seq(
    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")
  )

  lazy val formatSettings = ScalariformPlugin.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

  def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
  }
}

object Dependencies {
  import Dependency._

  val root = Seq(akkaActor, akkaMailboxesCommon withSources(), rabbit, commonsPool,
                 slf4jApi, Test.junit, Test.akkaActorTests, Test.scalatest)
}

object Dependency {

  object V {
    val Akka         = "2.0.1"
    val CommonsPool  = "1.5.6"
    val Rabbit       = "2.7.1"
    val Scalatest    = "1.6.1"
    val Slf4j        = "1.6.4"
  }

  val akkaMailboxesCommon = "com.typesafe.akka"           % "akka-mailboxes-common"  % V.Akka        // ApacheV2
  val akkaActor           = "com.typesafe.akka"           % "akka-actor"             % V.Akka        // ApacheV2
  val commonsPool         = "commons-pool"                % "commons-pool"           % V.CommonsPool // ApacheV2
  val rabbit              = "com.rabbitmq"                % "amqp-client"            % V.Rabbit      // Mozilla Public License
  val slf4jApi            = "org.slf4j"                   % "slf4j-api"              % V.Slf4j       // MIT

  object Test {
    val junit             = "junit"                       % "junit"               % "4.5"        % "test" // Common Public License 1.0
    val scalatest         = "org.scalatest"               %% "scalatest"          % V.Scalatest  % "test" // ApacheV2
    val scalacheck        = "org.scala-tools.testing"     %% "scalacheck"         % "1.9"        % "test" // New BSD
    val log4j             = "log4j"                       % "log4j"               % "1.2.14"     % "test" // ApacheV2
    val akkaActorTests    = "com.typesafe.akka"           % "akka-actor-tests"    % V.Akka       % "test" // ApacheV2
  }
}
