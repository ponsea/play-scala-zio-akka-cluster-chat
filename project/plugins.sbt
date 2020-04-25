resolvers ++= Seq(
  ("Seasar Repository" at "http://maven.seasar.org/maven2/").withAllowInsecureProtocol(true),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

addSbtPlugin("org.scalastyle"        %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scalameta"         % "sbt-scalafmt"           % "2.3.1")
addSbtPlugin("net.virtual-void"      % "sbt-dependency-graph"   % "0.10.0-RC1")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt"             % "6.2.3")
addSbtPlugin("com.typesafe.play"     % "sbt-plugin"             % "2.8.1")
