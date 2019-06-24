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

  private void cleanUpRawSentence() {
    processedSentence = rawSentence.toUpperCase().replaceAll("[^a-zA-Z ]+", "")
        .replaceAll("\\s+", " ");
  }

  private void removeInvalidWords() {
    List<String> validWords = new ArrayList<>();
    List<String> words = Arrays.asList(processedSentence.split(" "));

    words.forEach(w -> {
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
