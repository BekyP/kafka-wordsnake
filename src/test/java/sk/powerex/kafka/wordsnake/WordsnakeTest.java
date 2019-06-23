package sk.powerex.kafka.wordsnake;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class WordsnakeTest {

  private static final String TEST_SENTENCE = "TEST TOPIC CONSUME ERROR REST TEST TOP POST TURN NOTHING GAAAAA AAAA AAAAA AAAAAA AAAAAAA AAAAAAAA AAAAAA AAABBB BBB BBB BEEE EEEE";

  @Test
  void notJammedSnake() {
    Wordsnake snake = new Wordsnake(TEST_SENTENCE, false);
    snake.createSnakeMap();

    int charCount =
        TEST_SENTENCE.replaceAll("\\s+", "").length() - Arrays.asList(TEST_SENTENCE.split(" "))
            .size() + 1;

    assertThat(snake.getSnakeMap().getStringMap().replaceAll("\\s+", "").length())
        .isEqualTo(charCount);
  }

  @Test
  void possibleJammedSnake() {
    Wordsnake snake = new Wordsnake(TEST_SENTENCE, true);
    snake.createSnakeMap();

    int maxCharCount =
        TEST_SENTENCE.replaceAll("\\s+", "").length() - Arrays.asList(TEST_SENTENCE.split(" "))
            .size() + 1;

    assertThat(snake.getSnakeMap().getStringMap().replaceAll("\\s+", "").length())
        .isLessThanOrEqualTo(maxCharCount);

  }
}
