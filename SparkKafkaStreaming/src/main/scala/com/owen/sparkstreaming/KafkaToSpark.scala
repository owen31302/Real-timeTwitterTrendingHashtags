package com.owen.sparkstreaming

import Utilities._
import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage.StorageLevel

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka._
import com.datastax.spark.connector._


object KafkaToSpark {

  /** Working example of listening for log data from Kafka's testLogs topic on port 9092. */

  def main(args: Array[String]) {

    // Create the context with a 1 second batch size
    val sc = new SparkConf(true).set("spark.cassandra.connection.host", "127.0.0.1").setAppName("StreamingKafkaConsumer").setMaster("local[*]")
    val ssc = new StreamingContext(sc, Seconds(2))
    setupLogging()


    // hostname:port for Kafka brokers, not Zookeeper
    val kafkaParams = Map("metadata.broker.list" -> "localhost:9092", "auto.offset.reset" -> "smallest")
    // List of topics you want to listen for from Kafka
    val topics = List("twitter").toSet
    // Create our Kafka stream, which will contain (topic,message) pairs. We tack a
    // map(_._2) at the end in order to only get the messages, which contain individual lines of data.
    val lines = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    lines.foreachRDD((rdd, time) => {
      rdd.collect().foreach(println)
    })


    lines.foreachRDD((rdd, time) => {
      rdd.cache()
      println("Writing "+rdd.count()+" rows to Cassandra")
      rdd.saveToCassandra("test", "twitter", SomeColumns("id", "tweet"))
    })



    // Kick it off
    ssc.checkpoint("./checkpoint/")
    ssc.start()
    ssc.awaitTermination()

  }

}
