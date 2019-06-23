package sk.powerex.kafka.wordsnake;

import static java.util.stream.Collectors.toSet;

import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;
import sk.powerex.kafka.wordsnake.config.KafkaConsumerConfig;

@Data
@AllArgsConstructor
@Slf4j
public class WordsnakeConsumer {

  private KafkaConfig config;
  private KafkaProperties kafkaProperties;
  private KafkaConsumerConfig kafkaConsumerConfig;


  void consume() {
    KafkaConsumer<String, String> consumer = createKafkaConsumer();

    Set<TopicPartition> topicPartitions = consumer.partitionsFor(config.getOutputProcessedTopic()).stream()
        .map(p -> new TopicPartition(p.topic(), p.partition()))
        .collect(toSet());

    consumer.assign(topicPartitions);
    consumer.seekToBeginning(topicPartitions);

    try {
      while (!kafkaConsumerConfig.isTest()) {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

        records.forEach(r -> {
          try {
            Path file = Paths.get(kafkaConsumerConfig.getOutputFile());
            Files
                .write(Paths.get(kafkaConsumerConfig.getOutputFile()), prepareToWrite(r).getBytes(),
                    Files.exists(file) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
          } catch (IOException e) {
            log.error(e.getMessage());
          }


            /*
            log
            .info("{}: offset = {}, key = {}, value = {}", config.getOutputTopic(), r.offset(),
                r.key(),
                r.value()));*/
          //TODO fix errors
          Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        });
      }
    } catch (WakeupException e) {
      log.error(e.getMessage());
    } finally {
      consumer.close();
    }
  }

  private String prepareToWrite(ConsumerRecord<String, String> record) {
    return String
        .format("key: %s, snake: %n%s %n -----------------%n", record.key(), record.value());
  }

  private <K, V> KafkaConsumer<K, V> createKafkaConsumer() {
    Properties p = new Properties();
    p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    p.put(ConsumerConfig.GROUP_ID_CONFIG, config.getApplicationId());
    p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    return new KafkaConsumer<>(p);
  }
}
