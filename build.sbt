lazy val akkaHttpVersion = "10.2.0"
lazy val akkaVersion = "2.6.8"
lazy val xchangeVersion = "5.0.1"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "it.softfork",
      scalaVersion := "2.13.3"
    )
  ),
  name := "crypto-trades-export",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    // logging
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    // crypto exchange APIs
    "org.knowm.xchange" % "xchange-core" % xchangeVersion,
    "org.knowm.xchange" % "xchange-coinmarketcap" % xchangeVersion,
    "org.knowm.xchange" % "xchange-examples" % xchangeVersion,
    "org.knowm.xchange" % "xchange-stream-core" % xchangeVersion,
    "org.knowm.xchange" % "xchange-stream-binance" % xchangeVersion,
    // csv tools
    "com.github.tototoshi" %% "scala-csv" % "1.3.6",
    // for parsing command line argument
    "org.rogach" %% "scallop" % "3.5.0",
    // test relevant
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test
  )
)

fork in run := true
