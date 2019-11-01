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
  //logs
  "net.logstash.logback" % "logstash-logback-encoder" % "6.1",
  "ch.qos.logback"       % "logback-classic"          % "1.2.3",
  "org.slf4j"            % "jul-to-slf4j"             % "1.7.28",
  "org.codehaus.janino"  % "janino"                   % "3.1.0",
  //img proc
  "com.sksamuel.scrimage" %% "scrimage-core" % "3.0.0-alpha4",
  "com.sksamuel.scrimage" %% "scrimage-io-extra" % "3.0.0-alpha4",
  "com.sksamuel.scrimage" %% "scrimage-filters" % "3.0.0-alpha4",
  "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "1.1.1",
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "1.1.1",
  //akka
  "com.typesafe.akka" %% "akka-stream"          % "2.5.25",
  "io.spray" %%  "spray-json" % "1.3.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  //misc
  "org.typelevel"  %% "squants"  % "1.3.0",
  "org.typelevel"         %% "cats-core"  % "2.0.0-RC2",
  "com.github.pureconfig" %% "pureconfig" % "0.12.1",
  //test
  "org.scalatest"          %% "scalatest"         % "3.0.5"   % Test,
)

enablePlugins(JavaAppPackaging)
