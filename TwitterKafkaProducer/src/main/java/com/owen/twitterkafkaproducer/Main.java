package com.owen.twitterkafkaproducer;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;
import twitter4j.conf.*;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import kafka.producer.KeyedMessage;

/**
 * A Kafka Producer that gets tweets on certain keywords
 * from twitter datasource and publishes to a kafka topic
 *
 * Arguments: <comsumerKey> <consumerSecret> <accessToken> <accessTokenSecret> <topic-name> <keyword_1> ... <keyword_n>
 * <comsumerKey>	- Twitter consumer key
 * <consumerSecret>  	- Twitter consumer secret
 * <accessToken>	- Twitter access token
 * <accessTokenSecret>	- Twitter access token secret
 * <topic-name>		- The kafka topic to subscribe to
 * <keyword_1>		- The keyword to filter tweets
 * <keyword_n>		- Any number of keywords to filter tweets
 *
 * More discussion at stdatalabs.blogspot.com
 *
 * @author Sachin Thirumala
 */

public class Main {
    public static void main(String[] args) throws Exception {
        final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);

        if (args.length != 1) {
            System.out.println("please put a argument of the path of twitter login info.");
            return;
        }

        String consumerKey = "";
        String consumerSecret = "";
        String accessToken = "";
        String accessTokenSecret = "";

//        System.out.println(args[0]);
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        for(int i=0; i<4; i++) {
          String line = br.readLine();
          String[] word = line.split(",");
          switch (i) {
              case 0:
                  consumerKey = word[1];
                  break;
              case 1:
                  consumerSecret = word[1];
                  break;
              case 2:
                  accessToken = word[1];
                  break;
              case 3:
                  accessTokenSecret = word[1];
                  break;
          }
        }

        // Set twitter oAuth tokens in the configuration
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);

        // Create twitterstream using the configuration
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {

            public void onStatus(Status status) {
                if(status.getUser().getLocation() != null) {
                    queue.offer(status);
                    System.out.println(status);
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }


            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId);
            }

            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);

        // Filter keywords[]
        FilterQuery query = new FilterQuery().track(new String[]{"#"});

//        double[][] location = new double[2][2];
//        location[0][0] = -122.75;
//        location[0][1] = 36.8;
//        location[1][0] = -121.75;
//        location[1][1] = 37.8;
//        FilterQuery query = new FilterQuery().locations(location);

        twitterStream.filter(query);

        // Thread.sleep(5000);

        // Add Kafka producer config settings
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092");
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
//        int i = 0;
        int j = 0;

        // poll for new tweets in the queue. If new tweets are added, send them
        // to the topic
        while (true) {
            Status tweet = queue.poll();

            if (tweet == null) {
                Thread.sleep(100);
//                 i++;
            } else {
                System.out.println(tweet.getText());
                System.out.println("--------------------");
                producer.send(new ProducerRecord<String, String>("twitter", Integer.toString(j++), tweet.getUser().getLocation()+"-------"+tweet.getText()));
            }
        }
//         producer.close();
//         Thread.sleep(500);
//         twitterStream.shutdown();
    }

}