package sk.powerex.kafka.wordsnake.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Sentence {

  private Character lastCharacterOfValidWord;
  private final String rawSentence;
  private String processedSentence;

  // only words with upper case letters followed by space are allowed, not other characters
  private void cleanUpRawSentence() {
    processedSentence = rawSentence.toUpperCase().replaceAll("[^A-Z ]+", "")
        .replaceAll("\\s+", " ");
  }

  /*
   removes words that can`t be joined to snake,
   for example sentence 'TEST ONE TOPIC CONSUME' will become 'TEST TOPIC CONSUME'
    */
  private void removeInvalidWords() {
    List<String> validWords = new ArrayList<>();
    List<String> words = Arrays.asList(processedSentence.split(" "));

    words.forEach(w -> {
      // checks if last character of previous word is same as first character of current word
      if (lastCharacterOfValidWord == null || lastCharacterOfValidWord == w.charAt(0)) {
        validWords.add(w);
        lastCharacterOfValidWord = w.charAt(w.length() - 1);
      }
    });

    processedSentence = String.join(" ", validWords);
  }

  public void processSentence() {
    cleanUpRawSentence();
    removeInvalidWords();
  }
}
