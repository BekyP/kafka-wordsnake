package sk.powerex.kafka.wordsnake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.event.Level;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;
import sk.powerex.kafka.wordsnake.utils.Sentence;
import sk.powerex.kafka.wordsnake.utils.SparseLog;

@Data
@Slf4j
@Builder
@AllArgsConstructor
public class WordsnakeCreator {

  private static final StreamsBuilder streamsBuilder = new StreamsBuilder();
  private final KafkaConfig config;
  private final KafkaStreamsConfiguration kafkaStreamsConfiguration;

  void setupTopology() {

    KStream<String, String> wordsnakeStream = streamsBuilder
        .stream(config.getInputTopic())
        .mapValues(Object::toString)
        .peek((k, v) -> log.debug("{} - value: {}", config.getInputTopic(), v))
        .transform(SentenceTransformer::new);

    handleInvalidSentences(wordsnakeStream.filterNot((k, v) -> validateSentence(v)));

    handleValidSentences(wordsnakeStream.filter((k, v) -> validateSentence(v)));

    KafkaStreams streams = new KafkaStreams(streamsBuilder.build(),
        kafkaStreamsConfiguration.asProperties());

    streams.start();
  }

  private void handleValidSentences(KStream<String, String> stream) {
    // raw output - only valid words, not snake
    stream.peek((k, v) -> SparseLog.log(config.getOutputRawTopic(), k, v, config.getLogDensity(),
        Level.INFO))
        .to(config.getOutputRawTopic());

    // processed output - snake
    stream.transform(WordsnakeTransformer::new)
        .peek(
            (k, v) -> SparseLog.log(config.getOutputProcessedTopic(), k, v, config.getLogDensity(),
                Level.DEBUG))
        .to(config.getOutputProcessedTopic());
  }

  private void handleInvalidSentences(KStream<String, String> stream) {
    // error output - non valid sentences (not possible to construct snake, only 1 word, ...)
    stream.filterNot((k, v) -> validateSentence(v))
        .peek((k, v) -> SparseLog.log(config.getErrorTopic(), k, v, config.getLogDensity(),
            Level.INFO))
        .to(config.getErrorTopic());
  }

  private boolean validateSentence(String sentence) {
    return sentence.split(" ").length != 1;
  }

  private class SentenceTransformer implements
      Transformer<Object, String, KeyValue<String, String>> {

    @Override
    public void init(ProcessorContext processorContext) {
      // empty init, not working with state stores or context
    }

    @Override
    public KeyValue<String, String> transform(Object o, String value) {
      Sentence sentence = new Sentence(value);
      sentence.processSentence();
      return new KeyValue<>(value, sentence.getProcessedSentence());
    }

    @Override
    public void close() {
      // empty close
    }
  }

  private class WordsnakeTransformer implements
      Transformer<String, String, KeyValue<String, String>> {

    @Override
    public void init(ProcessorContext processorContext) {
      // empty init, not working with state stores or context
    }

    @Override
    public KeyValue<String, String> transform(String key, String value) {
      return new KeyValue<>(key,
          new Wordsnake(value, config.getAllowJammedSnake()).createSnakeMap().getStringMap());
    }

    @Override
    public void close() {
      // empty close
    }
  }
}
