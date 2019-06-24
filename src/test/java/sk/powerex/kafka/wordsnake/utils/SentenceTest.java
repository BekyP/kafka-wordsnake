package sk.powerex.kafka.wordsnake.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class SentenceTest {


  @Test
  void processValidSentece() {
    String testInput = "TEST TOPIC *_SEND !!COnsUME##";
    Sentence sentence = new Sentence(testInput);
    String expected = "TEST TOPIC CONSUME";
    sentence.processSentence();
    assertThat(sentence.getProcessedSentence()).isEqualTo(expected);
  }

  @Test
  void processInvalidSentence() {
    String testInput = "TEST dsfsd *_SEdsffsdND !!OnsUME##";
    Sentence sentence = new Sentence(testInput);
    String expected = "TEST";

    sentence.processSentence();
    assertThat(sentence.getProcessedSentence()).isEqualTo(expected);
  }
}
