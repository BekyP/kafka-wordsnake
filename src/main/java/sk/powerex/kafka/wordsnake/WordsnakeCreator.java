package sk.powerex.kafka.wordsnake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;
import sk.powerex.kafka.wordsnake.utils.Sentence;

@Data
@Slf4j
@Builder
@AllArgsConstructor
public class WordsnakeCreator {

  private static final StreamsBuilder streamsBuilder = new StreamsBuilder();
  private final KafkaConfig config;
  private final KafkaStreamsConfiguration kafkaStreamsConfiguration;


  void setupTopology() {

    KStream<String, String> wordsnakeStream = streamsBuilder.stream(config.getInputTopic())
        .peek((k, v) -> log.info("data: " + k + " - " + v))
        .mapValues(Object::toString)
        .map((k, v) -> new KeyValue<>(v, new Sentence().getProcessedSentence(v)));

    wordsnakeStream.filter((k, v) -> !validateSentence(v)).to(config.getErrorTopic());

    KStream<String, String> outputStream = wordsnakeStream.filter((k, v) -> validateSentence(v));

    outputStream.to(config.getOutputRawTopic());

    outputStream.map((k, v) -> new KeyValue<>(k,
        new Wordsnake(v, config.getAllowJammedSnake()).createSnakeMap().getStringMap()))
        .to(config.getOutputProcessedTopic());

    KafkaStreams streams = new KafkaStreams(streamsBuilder.build(),
        kafkaStreamsConfiguration.asProperties());

    streams.start();
  }

  private boolean validateSentence(String sentence) {
    return sentence.split(" ").length != 1;
  }

}
