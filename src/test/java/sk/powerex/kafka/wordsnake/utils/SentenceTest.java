package sk.powerex.kafka.wordsnake.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class SentenceTest {

  @Test
  void processValidSentece(){
    Sentence sentence = new Sentence();
    String testInput = "TEST TOPIC *_SEND !!COnsUME##";
    String expected = "TEST TOPIC CONSUME";
    String actual = sentence.getProcessedSentence(testInput);
    assertThat(actual).isEqualTo(expected);

  }

  @Test
  void processInvalidSentence(){
    Sentence sentence = new Sentence();
    String testInput = "TEST dsfsd *_SEdsffsdND !!OnsUME##";
    String expected = "TEST";

    String actual = sentence.getProcessedSentence(testInput);
    assertThat(actual).isEqualTo(expected);
  }
}
