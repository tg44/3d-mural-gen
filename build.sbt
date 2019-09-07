name := "mural-gen"

version := "0.1"

scalaVersion := "2.12.8"


scalacOptions in Compile ++= Seq(
  //"-Xlog-implicits",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xmax-classfile-name", "110",
  "-language:implicitConversions",
  "-language:postfixOps"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"          % "2.5.25",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.typelevel"  %% "squants"  % "1.3.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "6.1",
  "ch.qos.logback"       % "logback-classic"          % "1.2.3",
  "org.slf4j"            % "jul-to-slf4j"             % "1.7.28",
  "org.codehaus.janino"  % "janino"                   % "3.1.0"
)
