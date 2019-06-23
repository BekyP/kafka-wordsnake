package sk.powerex.kafka.wordsnake.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.consumer")
public class KafkaConsumerConfig {

  /**
   * output file with snakes
   */
  private String outputFile;

  /**
   * consumer id
   */

  private String consumerId = "wordsnake-consumer";
  /**
   * test
   */

  private boolean test = false;
}
