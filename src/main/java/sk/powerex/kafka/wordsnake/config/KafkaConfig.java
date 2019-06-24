package sk.powerex.kafka.wordsnake.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka properties
 */
@Component
@ConfigurationProperties("app.streams-config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaConfig {

  /**
   * kafka application id
   */
  private String applicationId;

  /**
   * input topic
   */
  private String inputTopic = "input";

  /**
   * output raw topic
   */
  private String outputRawTopic = "output_raw";

  /**
   * output processed topic
   */
  private String outputProcessedTopic = "output_processed";

  /**
   * error topic
   */
  private String errorTopic = "error";

  /**
   * allow jammed snake
   */
  private Boolean allowJammedSnake = false;

  /**
   * log density
   */

  private int logDensity = 1000;
}
