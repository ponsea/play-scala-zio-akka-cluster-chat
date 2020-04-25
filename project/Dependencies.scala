import sbt._

object Dependencies {
  val scalaTest         = "org.scalatest"          %% "scalatest"          % "3.0.8"
  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0"
  object ZIO {
    val version = "1.0.0-RC18-2"
    val core    = "dev.zio" %% "zio" % version
    val streams = "dev.zio" %% "zio-streams" % version
    val test    = "dev.zio" %% "zio-test" % version
    val testSbt = "dev.zio" %% "zio-test-sbt" % version
  }
  val zioAkkaCluster            = "dev.zio"      %% "zio-akka-cluster"            % "0.1.12"
  val zioInterOpReactiveStreams = "dev.zio"      %% "zio-interop-reactivestreams" % "1.0.3.5-RC6"
  val zioLoggingSlf4j           = "dev.zio"      %% "zio-logging-slf4j"           % "0.2.5"
  val enumeratum                = "com.beachape" %% "enumeratum"                  % "1.5.15"
  object Akka {
    val version         = "2.6.3"
    val actor           = "com.typesafe.akka" %% "akka-actor" % version
    val stream          = "com.typesafe.akka" %% "akka-stream" % version
    val clusterTools    = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val clusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % version
    val clusterTyped    = "com.typesafe.akka" %% "akka-cluster-typed" % version
    val slf4j           = "com.typesafe.akka" %% "akka-slf4j" % version
  }
  val refined            = "eu.timepit"        %% "refined"             % "0.9.13"
  val playSlick          = "com.typesafe.play" %% "play-slick"          % "5.0.0"
  val mysqlConnectorJava = "mysql"             % "mysql-connector-java" % "8.0.19"
  val h2                 = "com.h2database"    % "h2"                   % "1.4.200"
}
