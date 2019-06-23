package sk.powerex.kafka.wordsnake;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;

@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = {"input", "output", "error"})
@Slf4j
class WordSnakeCreatorTest {
/*
  private WordsnakeCreator wordSnakeCreator = WordsnakeCreator.builder().build();
  private static final String EXPECTED_SNAKE = "TEST TOPIC CONSUME";

  @Value("${spring.embedded.kafka.brokers}")
  private String kafkaBrokers;

  private KafkaConfig config = KafkaConfig.builder().inputTopic("input")
      .outputTopic("output").build();

  private static final String VALID_SENTENCE = "TEST TOPIC *_SEND !!COnsUME##";
  private static final String EXPECTED_OUTPUT_SENTENCE = "TEST TOPIC CONSUME";
  private static final String INVALID_SENTENCE = "invalid data!!!!";
  private static final String EXPECTED_ERROR_SENTENCE = "INVALID VALUE";
  private boolean inited = false;

  @BeforeEach
  void init() {
    System.out.println("inited:");
    System.out.println(inited);
    if (!inited) {
      // create a Kafka producer factory
      ProducerFactory<String, String> producerFactory =
          new DefaultKafkaProducerFactory<>(KafkaTestUtils.senderProps(kafkaBrokers));

      // create a Kafka template
      KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
      template.setDefaultTopic(config.getInputTopic()); // set the default topic to send to

      IntStream.range(0, 2).forEach(i -> template.sendDefault(VALID_SENTENCE));
      IntStream.range(0, 2).forEach(i -> template.sendDefault(INVALID_SENTENCE));

      log.info("test data sent");
    /*
    Consumer<String, String> consumer = getKafkaConsumer(config.getInputTopic());
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
    System.out.println("records:");
    System.out.println(records.count());
    records.forEach(
        r -> log.info("{}: offset = {}, key = {}, value = {}", config.getInputTopic(), r.offset(),
            r.key(),
            r.value()));
    }
    inited = true;
  }

  @Test
  void inputTopic() {
    Consumer<String, String> consumer = getKafkaConsumer(config.getInputTopic());
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

    records.forEach(
        r -> log.info("{}: offset = {}, key = {}, value = {}", config.getInputTopic(), r.offset(),
            r.key(),
            r.value()));

    assertThat(records.isEmpty()).isFalse();
    long validCount = StreamSupport
        .stream(records.spliterator(), false).filter(r -> r.value().equals(VALID_SENTENCE)).count();

    long invalidCount = StreamSupport
        .stream(records.spliterator(), false).filter(r -> r.value().equals(INVALID_SENTENCE))
        .count();

    assertThat(validCount).isEqualTo(2);
    assertThat(invalidCount).isEqualTo(2);

  }

  @Test
  void outputTopic() {
    Consumer<String, String> consumer = getKafkaConsumer(config.getOutputTopic());
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

    records.forEach(
        r -> log.info("{}: offset = {}, key = {}, value = {}", config.getOutputTopic(), r.offset(),
            r.key(),
            r.value()));

    assertThat(records.isEmpty()).isFalse();
    assertThat(records.count()).isEqualTo(2);
    StreamSupport.stream(records.spliterator(), false).forEach(r -> {
      assertThat(r.key()).isEqualTo(VALID_SENTENCE);
      assertThat(r.value()).isEqualTo(EXPECTED_OUTPUT_SENTENCE);
    });

  }


  private Consumer<String, String> getKafkaConsumer(String topic) {
    Map<String, Object> configs = new HashMap<>(
        KafkaTestUtils.consumerProps(kafkaBrokers, "test-group", "true"));
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(configs,
        new StringDeserializer(), new StringDeserializer()).createConsumer();
    consumer.subscribe(Collections.singleton(topic));

    return consumer;
  }
/*
  @Test
  void lowerCaseToUpperCase() {
    String input = "test TopiC CONSume";

    assertThat(wordSnakeCreator.cleanUpSentence(input)).isEqualTo(EXPECTED_SNAKE);
  }

  @Test
  void removeCharactersFromString() {
    String input = "#TEST, #TOPIC - CONSUME.";

    assertThat(wordSnakeCreator.cleanUpSentence(input)).isEqualTo(EXPECTED_SNAKE);

  }

  @Test
  void errorString() {
    String input = "JANO";

    assertThat(wordSnakeCreator.validateSentence(input)).isFalse();
  }

  @Test
  void validString() {
    String input = "test topic consume error ridiculous";

    assertThat(wordSnakeCreator.validateSentence(input)).isTrue();
  }

  @Test
  void removeInvalidWords() {
    String input = "JANO FERO OPASOK";
    String expected = "JANO OPASOK";

    assertThat(wordSnakeCreator.removeInvalidWords(input))
        .isEqualTo(expected);
  }

  /*
  @Configuration
  @Data
  class TestConfg {

    @Value("${spring.embedded.kafka.brokers}")
    private String kafkaBrokers;

    @Autowired
    private KafkaConfig config;

    @PostConstruct
    void sendTestRecordsToTopic() {
      // create a Kafka producer factory
      ProducerFactory<String, String> producerFactory =
          new DefaultKafkaProducerFactory<>(KafkaTestUtils.senderProps(kafkaBrokers));

      // create a Kafka template
      KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
      template.setDefaultTopic(config.getInputTopic()); // set the default topic to send to

      // send messages to test kafka topics, only 2 are within date range
      IntStream.range(0, 2).forEach(i -> template.sendDefault("TEST TOPIC *_SEND !!CONSUME##"));
      IntStream.range(0, 2).forEach(i -> template.sendDefault("invalid data!!!!"));


    }

  }*/
}
