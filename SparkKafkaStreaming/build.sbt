name := "SparkStreamingTwitter"

version := "1.0"

scalaVersion := "2.11.8"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

mainClass in (Compile, run) := Some("com.owen.sparkstreaming.AverageTweetLength")

mainClass in (Compile, packageBin) := Some("com.owen.sparkstreaming.AverageTweetLength")

libraryDependencies ++= {
  val sparkVer = "1.5.2"
  val twitterVer = "4.0.4"
  val twitterStreamVer = "1.6.3"
  Seq(
//    "org.apache.spark" %% "spark-core" % sparkVer % "provided" withSources(),
//    "org.apache.spark" % "spark-streaming_2.11" % sparkVer % "provided" withSources(),
    "org.apache.spark" %% "spark-core" % sparkVer,
    "org.apache.spark" % "spark-streaming_2.11" % sparkVer,
    "org.twitter4j" % "twitter4j-core" % twitterVer,
    "org.twitter4j" % "twitter4j-stream" % twitterVer,
    "org.apache.spark" %% "spark-streaming-twitter" % twitterStreamVer
  )
}

