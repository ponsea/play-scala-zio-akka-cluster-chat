import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.ponsea"
ThisBuild / organizationName := "ponsea"

val baseName = "play-scala-zio-akka-cluster-chat"

lazy val domain = (project in file("modules/domain"))
  .settings(
    name := s"${baseName}_domain",
    libraryDependencies ++= Seq(
        ZIO.core,
        ZIO.streams,
        zioLoggingSlf4j,
        enumeratum,
        refined,
        scalaTest   % Test,
        ZIO.test    % Test,
        ZIO.testSbt % Test
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
        ZIO.core,
        ZIO.streams,
        zioLoggingSlf4j,
        zioAkkaCluster,
        zioInterOpReactiveStreams,
        enumeratum,
        playSlick,
        mysqlConnectorJava,
        clusterSharding,
        refined,
        scalaTest         % Test,
        scalaTestPlusPlay % Test,
        ZIO.test          % Test,
        ZIO.testSbt       % Test,
        h2                % Test
      ),
    dependencyOverrides ++= Seq(
        Akka.clusterTools,
        Akka.clusterSharding
      ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(domain)

// `flyway-<DB Name>` for each database
lazy val `flyway-sample` = (project in file("tools/flyway/sample"))
  .enablePlugins(FlywayPlugin)
  .settings(
    name := s"${baseName}_flyway",
    libraryDependencies += mysqlConnectorJava,
    flywayDriver := "com.mysql.cj.jdbc.Driver",
    flywayUrl := "jdbc:mysql://localhost:3306/sample?allowPublicKeyRetrieval=true&useSSL=false",
    flywayUser := "sample",
    flywayPassword := "sample",
    flywaySchemas := Seq("sample"),
    flywayLocations := Seq(s"filesystem:${baseDirectory.value}/src/test/resources/")
  )

lazy val root = (project in file("."))
  .settings(
    name := baseName
  )
  .aggregate(
    domain,
    interfaces
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
