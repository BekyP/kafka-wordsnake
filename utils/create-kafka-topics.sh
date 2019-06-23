#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

topics=("input" "error" "output_raw" "output_processed")

$DIR/wait-for-kafka-start.sh

for t in "${topics[@]}"
do
	echo "creating topic $t"
	#TODO pipe error output
	$DIR/create-kafka-topic.sh "zookeeper:2181" "$t"
done