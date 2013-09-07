import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := """scala-rest-interpreter"""

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

libraryDependencies ++= Seq(
  "org.scala-lang"      % "scala-compiler"   % "2.10.2",
  "org.scala-lang"      % "scala-library"    % "2.10.2",
  "com.typesafe.akka"  %% "akka-actor"       % "2.2.0",
  "io.spray"            % "spray-can"        % "1.2-20130712",
  "io.spray"            % "spray-routing"    % "1.2-20130712",
  "org.specs2"         %% "specs2"           % "1.14"         % "test",
  "io.spray"            % "spray-testkit"    % "1.2-20130712"  % "test",
  "com.typesafe.akka"  %% "akka-testkit"     % "2.2.0"        % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

test in assembly := {}