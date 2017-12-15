package com.owen.sparkstreaming

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import Utilities._
import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage.StorageLevel
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka._
import com.datastax.spark.connector._
import org.joda.time.DateTime


object KafkaToSpark {

  /** Working example of listening for log data from Kafka's testLogs topic on port 9092. */

  def main(args: Array[String]) {

    // Create the context with a 1 second batch size
    val sc = new SparkConf(true)
    sc.set("spark.cassandra.connection.host", "127.0.0.1")
    sc.setAppName("StreamingKafkaConsumer")
    sc.setMaster("local[*]")
    val ssc = new StreamingContext(sc, Seconds(300))
    setupLogging()


    // hostname:port for Kafka brokers, not Zookeeper
    val kafkaParams = Map(
      "bootstrap.servers" -> "localhost:9092",
      "group.id" -> "twitterTestingGroup",
      "auto.offset.reset" -> "largest",
      "zookeeper.sync.time.ms" -> "200",
      "enable.auto.commit" -> "true",
      "auto.commit.interval.ms" -> "100"
    )

    // List of topics you want to listen for from Kafka
    val topics = List("twitter").toSet
    // Create our Kafka stream, which will contain (topic,message) pairs. We tack a
    // map(_._2) at the end in order to only get the messages, which contain individual lines of data.
    val lines = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics).map(_._2)

//    lines.foreachRDD((rdd, time) => {
//      rdd.collect().foreach(println)
//    })

    // states sequence
    val states = Seq("AL", "AK", "AZ", "AR","CA", "CO", "CT", "DE", "FL", "GA","HI","ID",
      "IL","IN","IA","KS","KY","LA","ME","MD","MA","MI", "MN","MS", "MO","MT","NE","NV",
      "NH", "NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV", "WI","WY")
    //    lines.foreachRDD((rdd, time) => {
    //      rdd.collect().foreach(println)
    //    })

    /* 1 parse the state and filter
     * 2 parse the hashtags
     * 3 merge them
     */
    lines.foreachRDD((rdd, time) => {
      val tweets = rdd.map(x => {
        val splits = x.split("-------")
        (splits(0), splits(1))
      })

      val filtered = tweets.filter(line => {
        val address:Array[String] = line._1.split(" ")
        address.exists(states.contains)
      })

      val stateTweet = filtered.map(line => {
        val address = line._1.split(" ")
        var finalState = line._1
        for (state <- states) {
          if (address.contains(state)) {
            finalState = state
          }
        }
        (finalState, line._2)
      })

      val stateT = stateTweet.map(line => {
        val tw = line._2.split(" ")
        var tags: Seq[String] = Nil
        for (word <- tw) {
          if (word.startsWith("#") && word.length() > 1 && !word.contains("https")) {
            tags = tags :+ word
          }
        }
        (line._1, tags)
      })
      val stateAndTagList = stateT.filter(line => line._2.size > 0).groupByKey()

      val stateAndTag = stateAndTagList.map(line => {
        var combinedTag:List[String] = Nil
        for (list <- line._2) {
          combinedTag = List.concat(combinedTag,list)
        }
        val newValue = combinedTag.groupBy(identity).mapValues(_.size)
        val mostPopular = newValue.maxBy(_._2)
        (line._1, mostPopular)
      })

      // returns org.joda.time.DateTime = 2009-04-27T13:25:42.659-08:00
      val timestamp = DateTime.now

      // map the time stamp and the states info
      val finalTupleHelper = stateAndTag.map(line => (timestamp,line))
      val finalTuple = finalTupleHelper.groupByKey()

      // print the final RDD
      finalTuple.foreach(line => {
        println(line._1 + ":")
        for (state <- line._2) {
          println(state._1 + "'s the most popular tag : " + state._2)
        }
      })

      // save the final RDD into Cassandra
      finalTuple.saveToCassandra("twitterkeyspace", "twitter", SomeColumns("time", "status"))
    })

    // Kick it off
    //ssc.checkpoint("./checkpoint/")
    ssc.start()
    ssc.awaitTermination()

  }

}
