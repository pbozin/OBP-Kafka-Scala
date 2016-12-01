name := "OBPKafkaSandbox"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in (Compile, packageBin) := Some("com.md.OBPKafkaSandbox.core.OBPKafkaSandbox")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

resolvers ++= Seq(
                   "obp-git-based-repo" at "https://raw.githubusercontent.com/OpenBankProject/OBP-M2-REPO/master",
                   Resolver.sonatypeRepo("snapshots")
                 )

val akkaVersion  = "2.4.8"
val scalaTestV   = "2.2.6"
val obpjvmVersion = "2016.11-RC2-SNAPSHOT"

libraryDependencies ++= {

  Seq(
    "com.typesafe.akka"            %% "akka-actor"                  % akkaVersion     exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka"            %% "akka-slf4j"                  % akkaVersion     exclude ("org.scala-lang" , "scala-library"),
    "org.specs2"                   %% "specs2-core"                 % "2.4.17"        % "test",
    "com.typesafe.akka"            %% "akka-testkit"                % akkaVersion     % "test",
    "org.scalatest"                %% "scalatest"                   % scalaTestV      % "test",
    "org.apache.kafka"             %% "kafka"                       % "0.10.0.1",
    "com.google.guava"              % "guava"                       % "19.0",
    "net.liftweb"                  %% "lift-webkit"                 % "2.6.3"           % "compile->default",
    "net.liftweb"                  %% "lift-json"                   % "2.6.3",
    "com.tesobe.obp"               % "obp-ri-core"                  % obpjvmVersion,
    "com.tesobe.obp"               % "obp-ri-kafka"                 % obpjvmVersion,
    "com.tesobe.obp"               % "obp-ri-transport"             % obpjvmVersion
  )
}
