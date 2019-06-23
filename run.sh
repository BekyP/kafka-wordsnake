#!/bin/bash

# run kafka
docker-compose up -d

# create topics, send test data
docker exec kafka-wordsnake_broker_1 /utils/create-kafka-topics.sh
docker exec kafka-wordsnake_broker_1 /utils/send-test-data.sh

# build app, image and run
mvn clean package
docker build -t kafka_wordsnake .
docker run -d -p 8080:8080 -v /tmp/wordsnakes:/tmp/wordsnakes --network="kafka-wordsnake_default"  --name kafka_wordsnake kafka_wordsnake
