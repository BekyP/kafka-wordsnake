#!/bin/bash

kafka-topics \
  --zookeeper $1 \
  --create \
  --replication-factor 1 \
  --partitions 1 \
  --topic $2 \
  --config min.insync.replicas=2 \
  --config unclean.leader.election.enable=false
