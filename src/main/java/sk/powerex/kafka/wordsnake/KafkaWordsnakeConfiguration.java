package sk.powerex.kafka.wordsnake;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import sk.powerex.kafka.wordsnake.config.KafkaConfig;
import sk.powerex.kafka.wordsnake.config.KafkaConsumerConfig;

@Configuration
@EnableKafka
@EnableKafkaStreams
@Slf4j
@AllArgsConstructor
public class KafkaWordsnakeConfiguration {

  private KafkaConfig config;

  private KafkaConsumerConfig consumerConfig;

  private final KafkaProperties kafka;


  @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
  public KafkaStreamsConfiguration kStreamsConfigs() {
    Map<String, Object> p = ImmutableMap.<String, Object>builder()
        .put(StreamsConfig.CLIENT_ID_CONFIG, kafka.getClientId())
        .put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers())
        .put(StreamsConfig.APPLICATION_ID_CONFIG, config.getApplicationId())
        .put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerde.class.getName())
        .put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, StringSerde.class.getName())
        .build();

    log.info("Streams Configuration: \n\t{}",
        p.entrySet().stream().map(Entry::toString).collect(joining("\n\t")));

    return new KafkaStreamsConfiguration(p);
  }


  @Bean
  <K, V> KafkaConsumer<K, V> kafkaConsumer() {
    Properties p = new Properties();
    p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    p.put(ConsumerConfig.GROUP_ID_CONFIG, config.getApplicationId());
    p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    return new KafkaConsumer<>(p);
  }


  @Bean
  WordsnakeCreator wordsnakeCreator() {
    log.info("creating wordsnakeCreator with configuration: " + config);
    WordsnakeCreator creator = new WordsnakeCreator(config, kStreamsConfigs());
    creator.setupTopology();

    return creator;
  }


  @Bean
  WordsnakeConsumer wordsnakeConsumer() {
    WordsnakeConsumer wordsnakeConsumer = new WordsnakeConsumer(config, consumerConfig,
        kafkaConsumer());
    wordsnakeConsumer.consume();

    return wordsnakeConsumer;
  }
}
