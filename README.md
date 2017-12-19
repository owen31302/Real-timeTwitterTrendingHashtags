# Real-time Twitter Trending Hashtags

This project is to provide a visualization web app for the twitter hashtag trends around the world. 

## System Overview


## Authors
Yu-Cheng Lin, TzuChien Wang, Xinzhu Cai

## Modules
##### TwitterKafkaProducer  
Fetch twitter streaming data from twitter and ingest into Kafka as an Kafka producer.

##### SparkKafkaStreaming
Streaming processing from Kafka and save the result to Cassandra.

##### server
Include front end/back end/database cache layer.