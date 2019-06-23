package sk.powerex.kafka.wordsnake;

import static org.assertj.core.api.Assertions.assertThat;

import com.landoop.kafka.testing.KCluster;
import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class WordsnakeKafkaIntegrationTest {

  private static KCluster kCluster;

  private static KafkaConfig config = new KafkaConfig();

  @Value("${spring.kafka.client-id}")
  private String clientId;

  private static final String VALID_SENTENCE = "TEST TOPIC *_SEND !!COnsUME##";
  private static final String EXPECTED_OUTPUT_SENTENCE = "TEST TOPIC CONSUME";
  private static final String INVALID_SENTENCE = "invalid value!!!!";
  private static final String EXPECTED_ERROR_SENTENCE = "INVALID";

  private static final String TEST_KAFKA_BROKERS_PROPERTY = "test.kafka.brokers";


  @BeforeAll
  static void setup() {
    // toto je ta kniznica schopna pracovat s avro formatom
    kCluster = new KCluster(3, false, AvroCompatibilityLevel.BACKWARD);

    System.setProperty(TEST_KAFKA_BROKERS_PROPERTY, kCluster.BrokersList());

    Stream.of(config.getInputTopic(),
        config.getOutputRawTopic(),
        config.getOutputProcessedTopic(),
        config.getErrorTopic())
        .peek(topic -> log.info("Topic created: {}", topic))
        .forEach(topic -> kCluster.createTopic(topic, 1, 3));

    sendTestData();
  }

  @AfterAll
  static void cleanExit() {
    try {
      kCluster.close();
    } catch (Exception e) {
      log.warn("cleanExit() error closing cluster {}", e.toString());
    }
  }

  private static void sendTestData() {
    Map<String, Object> props;
    props = KafkaTestUtils
        .senderProps(System.getProperty(TEST_KAFKA_BROKERS_PROPERTY));

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    ProducerFactory<String, String> producerFactory =
        new DefaultKafkaProducerFactory<>(props);

    // create a Kafka template
    KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
    template.setDefaultTopic(config.getInputTopic()); // set the default topic to send to

    IntStream.range(0, 2).forEach(i -> template.sendDefault(VALID_SENTENCE));
    IntStream.range(0, 2).forEach(i -> template.sendDefault(INVALID_SENTENCE));

    log.info("test data sent");

  }

  private Consumer<String, String> getKafkaConsumer(String topic) {
    Map<String, Object> configs = new HashMap<>(
        KafkaTestUtils
            .consumerProps(System.getProperty(TEST_KAFKA_BROKERS_PROPERTY), clientId, "true"));
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(configs,
        new StringDeserializer(), new StringDeserializer()).createConsumer();
    consumer.assign(Collections.singleton(new TopicPartition(topic, 0)));
    consumer.seekToBeginning(Collections.singleton(new TopicPartition(topic, 0)));

    return consumer;
  }

  @Test
  void testInputTopic() {
    ConsumerRecords<String, String> records = getKafkaConsumer(config.getInputTopic())
        .poll(Duration.ofMillis(100));

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
  void testOutputRawTopic() {
    ConsumerRecords<String, String> records = getKafkaConsumer(config.getOutputRawTopic())
        .poll(Duration.ofMillis(100));

    records.forEach(r -> log
        .info("{}: offset = {}, {} = {}", config.getOutputRawTopic(), r.offset(), r.key(),
            r.value()));

    assertThat(records.isEmpty()).isFalse();
    assertThat(records.count()).isEqualTo(2);
    StreamSupport.stream(records.spliterator(), false).forEach(r -> {
      assertThat(r.key()).isEqualTo(VALID_SENTENCE);
      assertThat(r.value()).isEqualTo(EXPECTED_OUTPUT_SENTENCE);
    });

  }

  @Test
  void testOutputProcessedTopic() {
    ConsumerRecords<String, String> records = getKafkaConsumer(config.getOutputProcessedTopic())
        .poll(Duration.ofMillis(100));

    records.forEach(r -> log
        .info("{}: offset = {}, {} = {}", config.getOutputProcessedTopic(), r.offset(), r.key(),
            r.value()));

    assertThat(records.isEmpty()).isFalse();
    assertThat(records.count()).isEqualTo(2);
    StreamSupport.stream(records.spliterator(), false)
        .forEach(r -> assertThat(r.key()).isEqualTo(VALID_SENTENCE));

  }

  @Test
  void testErrorTopic() {
    ConsumerRecords<String, String> records = getKafkaConsumer(config.getErrorTopic())
        .poll(Duration.ofMillis(100));

    assertThat(records.isEmpty()).isFalse();
    assertThat(records.count()).isEqualTo(2);
    StreamSupport.stream(records.spliterator(), false).forEach(r -> {
      assertThat(r.key()).isEqualTo(INVALID_SENTENCE);
      assertThat(r.value()).isEqualTo(EXPECTED_ERROR_SENTENCE);
    });
  }
}
