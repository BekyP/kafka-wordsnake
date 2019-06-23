package sk.powerex.kafka.wordsnake;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private KafkaConfig config;

  @Autowired
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
  WordsnakeCreator wordsnakeCreator() {
    log.info("creating wordsnakeCreator with configuration: " + config);
    WordsnakeCreator creator = new WordsnakeCreator(config,  kStreamsConfigs());
    creator.setupTopology();

    return creator;
  }


  @Bean
  WordsnakeConsumer wordsnakeConsumer() {
    WordsnakeConsumer wordsnakeConsumer = new WordsnakeConsumer(config, kafka, consumerConfig);
    wordsnakeConsumer.consume();

    return wordsnakeConsumer;
  }
}
