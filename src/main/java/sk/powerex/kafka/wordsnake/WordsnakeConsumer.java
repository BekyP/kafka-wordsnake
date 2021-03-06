package sk.powerex.kafka.wordsnake;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.event.Level;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;
import sk.powerex.kafka.wordsnake.config.KafkaConsumerConfig;
import sk.powerex.kafka.wordsnake.utils.SparseLog;

@Data
@AllArgsConstructor
@Slf4j
public class WordsnakeConsumer {

  private final KafkaConfig config;
  private final KafkaConsumerConfig kafkaConsumerConfig;
  private final KafkaConsumer<String, String> consumer;

  void consume() {
    consumer.subscribe(Collections.singleton(config.getOutputProcessedTopic()));
    Path path = Paths.get(kafkaConsumerConfig.getOutputFile());

    try {
      while (!kafkaConsumerConfig.isTest()) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

        records.forEach(r -> {
          try {
            Files
                .write(path, prepareToWrite(r).getBytes(),
                    path.toFile().exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
          } catch (IOException e) {
            log.error("writing to file failed", e);
          }

          SparseLog.log(config.getOutputProcessedTopic(), r.key(), r.value(),
              kafkaConsumerConfig.getLogDensity(),
              Level.DEBUG);
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
}
