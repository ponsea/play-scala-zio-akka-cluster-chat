import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.ponsea"
ThisBuild / organizationName := "ponsea"

val baseName = "play-scala-zio-akka-cluster-chat"

lazy val domain = (project in file("modules/domain"))
  .settings(
    name := s"${baseName}_domain",
    libraryDependencies ++= Seq(
      zio.core,
      zio.streams,
      zioLoggingSlf4j,
      enumeratum,
      refined,
      scalaTest   % Test,
      zio.test    % Test,
      zio.testSbt % Test,
    ),
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val interfaces = (project in file("modules/interfaces"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    name := s"${baseName}_interfaces",
    libraryDependencies ++= Seq(
      zio.core,
      zio.streams,
      zioLoggingSlf4j,
      zioAkkaCluster,
      zioInterOpReactiveStreams,
      enumeratum,
      playSlick,
      clusterSharding,
      refined,
      scalaTest         % Test,
      scalaTestPlusPlay % Test,
      zio.test          % Test,
      zio.testSbt       % Test,
    ),
    dependencyOverrides ++= Seq(
      akka.clusterTools,
      akka.clusterSharding
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(domain)

lazy val root = (project in file("."))
  .settings(
    name := baseName
  )
  .aggregate(
    domain,
    interfaces
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
