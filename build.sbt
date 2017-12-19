name := "root-project"

version := "1.0"

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .aggregate(SparkKafkaStreaming, TwitterKafkaProducer)

lazy val SparkKafkaStreaming = (project in file("SparkKafkaStreaming"))

lazy val TwitterKafkaProducer = (project in file("TwitterKafkaProducer"))


// mainClass in root in Compile := (mainClass in web in Compile).value

// fullClasspath in web in Runtime ++= (fullClasspath in main in Runtime).value

// fullClasspath in root in Runtime ++= (fullClasspath in web in Runtime).value