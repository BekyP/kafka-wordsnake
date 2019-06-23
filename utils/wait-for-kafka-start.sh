#!/bin/bash

#TODO pipe error output
until kafka-topics --bootstrap-server broker:9092 --list | grep _confluent; do
  echo "Waiting for kafka to start"
  sleep 1
done