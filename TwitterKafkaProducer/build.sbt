name := "TwitterKafkaProducer"

version := "1.0"

javacOptions ++= Seq("-source", "1.8")



libraryDependencies ++= {
  val kafkaVer = "0.8.2.1"
  val twitter4jVer = "4.0.4"
  Seq(
    //https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.11/0.8.2.1
    "org.apache.kafka" %% "kafka" % kafkaVer,
    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    "org.twitter4j" % "twitter4j-core" % twitter4jVer,
    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-stream
    "org.twitter4j" % "twitter4j-stream" % twitter4jVer,
    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-async
    "org.twitter4j" % "twitter4j-async" % twitter4jVer
  )
}
        